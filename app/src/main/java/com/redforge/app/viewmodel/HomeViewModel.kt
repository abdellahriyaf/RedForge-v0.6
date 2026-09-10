package com.redforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redforge.app.data.datastore.ForgeSettings
import com.redforge.app.data.datastore.SettingsDataStore
import com.redforge.app.data.local.entities.Split
import com.redforge.app.data.local.entities.SplitDay
import com.redforge.app.data.local.entities.WorkoutSession
import com.redforge.app.data.repository.SplitRepository
import com.redforge.app.data.repository.WorkoutRepository
import com.redforge.app.domain.schedule.SplitScheduler
import com.redforge.app.domain.streak.StreakCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val activeSplit: Split? = null,
    val nextDay: SplitDay? = null,
    val inProgressSession: WorkoutSession? = null,
    val todayCompleted: Boolean = false,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val weekWorkouts: Int = 0,
    val weekSets: Int = 0,
    val weekVolume: Int = 0,
    val settings: ForgeSettings = ForgeSettings(),
    val scheduleNotStarted: Boolean = false,
    val loading: Boolean = true
)

private val STREAK_MILESTONES = listOf(7, 30, 100, 365)

class HomeViewModel(
    private val splitRepository: SplitRepository,
    private val workoutRepository: WorkoutRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _milestoneEvent = MutableStateFlow<Int?>(null)
    val milestoneEvent: StateFlow<Int?> = _milestoneEvent.asStateFlow()

    private val activeSplitWithDays: Flow<Pair<Split?, List<SplitDay>>> =
        splitRepository.observeActiveSplit().flatMapLatest { split ->
            if (split == null) {
                flowOf(null to emptyList())
            } else {
                splitRepository.observeDays(split.id).map { days -> split to days }
            }
        }

    val uiState: StateFlow<HomeUiState> = combine(
        activeSplitWithDays,
        workoutRepository.observeInProgressSession(),
        workoutRepository.observeAllSessions(),
        settingsDataStore.settingsFlow
    ) { splitAndDays, inProgress, allSessions, settings ->
        val activeSplit = splitAndDays.first
        val days = splitAndDays.second
        val today = System.currentTimeMillis()
        val scheduleAnchor = settings.scheduleAnchorStartMillis.takeIf {
            it != null && settings.scheduleAnchorSplitId == activeSplit?.id
        }
        val planned = SplitScheduler.plannedDayForDate(
            days,
            allSessions,
            today,
            scheduleAnchorStartMillis = scheduleAnchor
        )
        val scheduleNotStarted = activeSplit != null &&
            scheduleAnchor?.let { SplitScheduler.isBeforeAnchor(it, today) } == true
        val streak = StreakCalculator.compute(allSessions, nowMillis = today)
        val todayCompleted = allSessions.any { session ->
            session.completed &&
                session.splitDayId != null &&
                days.any { it.id == session.splitDayId } &&
                isSameCalendarDay(session.startedAt, today)
        }

        val weekStart = startOfWeekMillis(today)
        val weekSessions = allSessions.filter {
            it.completed && it.startedAt >= weekStart && it.startedAt <= today
        }
        val weekStats = workoutRepository.getSetStatsBetween(weekStart, today)

        HomeUiState(
            activeSplit = activeSplit,
            nextDay = planned,
            inProgressSession = inProgress,
            todayCompleted = todayCompleted,
            currentStreak = streak.current,
            longestStreak = streak.longest,
            weekWorkouts = weekSessions.size,
            weekSets = weekStats.setCount,
            weekVolume = weekStats.totalVolume.toInt(),
            settings = settings,
            scheduleNotStarted = scheduleNotStarted,
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch {
            uiState.filter { !it.loading }.collect { state ->
                val milestone = STREAK_MILESTONES.lastOrNull {
                    it <= state.currentStreak && it > state.settings.lastCelebratedMilestone
                }
                if (milestone != null) {
                    settingsDataStore.setLastCelebratedMilestone(milestone)
                    _milestoneEvent.value = milestone
                }
            }
        }
    }

    fun consumeMilestoneEvent() {
        _milestoneEvent.value = null
    }

    private fun isSameCalendarDay(firstMillis: Long, secondMillis: Long): Boolean {
        val a = Calendar.getInstance().apply { timeInMillis = firstMillis }
        val b = Calendar.getInstance().apply { timeInMillis = secondMillis }
        return a.get(Calendar.ERA) == b.get(Calendar.ERA) &&
            a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    private fun startOfWeekMillis(nowMillis: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = nowMillis }
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

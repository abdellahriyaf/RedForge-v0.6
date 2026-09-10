package com.redforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redforge.app.data.local.entities.SetEntry
import com.redforge.app.data.local.entities.WorkoutSession
import com.redforge.app.data.repository.ExerciseRepository
import com.redforge.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Summary row used by the History list. */
data class HistorySessionUi(
    val session: WorkoutSession,
    val setCount: Int,
    val workingSetCount: Int,
    val volume: Int
)

/** One exercise group inside a completed history session. */
data class HistoryExerciseUi(
    val exerciseId: Long,
    val name: String,
    val sets: List<SetEntry>
)

class HistoryViewModel(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    val sessions: StateFlow<List<HistorySessionUi>> =
        workoutRepository.observeCompletedHistoryStats()
            .map { rows ->
                rows.map { row ->
                    HistorySessionUi(
                        session = WorkoutSession(
                            id = row.sessionId,
                            splitDayId = null,
                            splitDayNameSnapshot = row.splitDayNameSnapshot,
                            startedAt = row.startedAt,
                            endedAt = row.endedAt,
                            completed = true
                        ),
                        setCount = row.setCount,
                        workingSetCount = row.workingSetCount,
                        volume = row.totalVolume.toInt()
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class HistoryDetailViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val sessionId: Long
) : ViewModel() {

    data class UiState(
        val session: WorkoutSession? = null,
        val exercises: List<HistoryExerciseUi> = emptyList(),
        val totalVolume: Int = 0,
        val loading: Boolean = true,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val session = workoutRepository.getSession(sessionId)
        if (session == null || !session.completed) {
            _uiState.value = UiState(loading = false, error = "That workout could not be found.")
            return
        }

        val sets = workoutRepository.getSetsOnce(session.id)
        val exerciseIds = sets.asSequence().map { it.exerciseId }.distinct().toList()
        val exerciseMap = exerciseIds.mapNotNull { id ->
            exerciseRepository.getById(id)?.let { id to it }
        }.toMap()

        val groups = sets
            .groupBy { it.exerciseId }
            .mapNotNull { (exerciseId, entries) ->
                val exercise = exerciseMap[exerciseId] ?: return@mapNotNull null
                HistoryExerciseUi(
                    exerciseId = exerciseId,
                    name = exercise.name,
                    sets = entries.sortedBy { it.setIndex }
                )
            }
            .sortedBy { it.sets.firstOrNull()?.loggedAt ?: Long.MAX_VALUE }

        _uiState.value = UiState(
            session = session,
            exercises = groups,
            totalVolume = sets.sumOf { if (it.completed && !it.isWarmup) it.weight * it.reps else 0.0 }.toInt(),
            loading = false
        )
    }

    fun updateSet(set: SetEntry, weight: Double, reps: Int, rpe: Float?) {
        if (weight < 0.0 || reps <= 0) return
        viewModelScope.launch {
            workoutRepository.updateSet(set.copy(weight = weight, reps = reps, rpe = rpe, isPersonalRecord = false))
            load()
        }
    }

    fun deleteSet(set: SetEntry) {
        viewModelScope.launch {
            workoutRepository.deleteSetAndReindex(set)
            load()
        }
    }
}

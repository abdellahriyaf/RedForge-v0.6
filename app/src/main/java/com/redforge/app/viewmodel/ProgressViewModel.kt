package com.redforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redforge.app.data.local.entities.Exercise
import com.redforge.app.data.local.entities.SetEntry
import com.redforge.app.data.repository.ExerciseRepository
import com.redforge.app.data.repository.WorkoutRepository
import com.redforge.app.domain.formulas.StrengthFormulas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


data class ExerciseProgressSummary(
    val exercise: Exercise,
    val bestEstimated1RM: Int,
    val totalVolumeAllTime: Int,
    val volumeChangePercent: Double,
    val sessionCount: Int
)

data class ExerciseTrendPoint(
    val sessionId: Long,
    val date: Long,
    val estimated1RM: Int,
    val volume: Int
)

class ProgressViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    val summaries: StateFlow<List<ExerciseProgressSummary>> = combine(
        exerciseRepository.observeAll(),
        workoutRepository.observeAllWorkingSets()
    ) { exercises, allWorkingSets ->
        val setsByExercise = allWorkingSets.groupBy { it.exerciseId }
        exercises.mapNotNull { exercise ->
            buildSummary(exercise, setsByExercise[exercise.id].orEmpty())
        }.sortedByDescending { it.totalVolumeAllTime }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun buildSummary(exercise: Exercise, sets: List<SetEntry>): ExerciseProgressSummary? {
        if (sets.isEmpty()) return null
        val sorted = sets.sortedBy { it.loggedAt }
        val midpoint = sorted.size / 2
        val splitPoint = maxOf(midpoint, 1)
        val earlierHalf = sorted.take(splitPoint)
        val recentHalf = sorted.drop(midpoint).ifEmpty { earlierHalf }
        val volumeChange = StrengthFormulas.percentChange(
            StrengthFormulas.totalVolume(earlierHalf),
            StrengthFormulas.totalVolume(recentHalf)
        )
        val sessionCount = sorted.asSequence().map { it.workoutSessionId }.distinct().count()
        return ExerciseProgressSummary(
            exercise = exercise,
            bestEstimated1RM = StrengthFormulas.displayRounded(StrengthFormulas.bestEstimated1RM(sets)),
            totalVolumeAllTime = StrengthFormulas.displayRounded(StrengthFormulas.totalVolume(sets)),
            volumeChangePercent = volumeChange,
            sessionCount = sessionCount
        )
    }
}

class ExerciseProgressDetailViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    private val exerciseId: Long
) : ViewModel() {

    data class UiState(
        val exercise: Exercise? = null,
        val points: List<ExerciseTrendPoint> = emptyList(),
        val bestEstimated1RM: Int = 0,
        val allTimeVolume: Int = 0,
        val loading: Boolean = true,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val exercise = exerciseRepository.getById(exerciseId)
        if (exercise == null) {
            _uiState.value = UiState(loading = false, error = "Exercise not found.")
            return
        }

        val sessions = workoutRepository.getCompletedSessionsBetween(0L, Long.MAX_VALUE)
            .associateBy { it.id }
        val groupedSets = workoutRepository.getCompletedSetsForExercise(exerciseId)
            .groupBy { it.workoutSessionId }

        val points = groupedSets.mapNotNull { (sessionId, sessionSets) ->
            val session = sessions[sessionId] ?: return@mapNotNull null
            ExerciseTrendPoint(
                sessionId = sessionId,
                date = session.startedAt,
                estimated1RM = StrengthFormulas.displayRounded(
                    StrengthFormulas.bestEstimated1RM(sessionSets)
                ),
                volume = StrengthFormulas.displayRounded(
                    sessionSets.sumOf { it.weight * it.reps }
                )
            )
        }.sortedBy { it.date }

        _uiState.value = UiState(
            exercise = exercise,
            points = points,
            bestEstimated1RM = points.maxOfOrNull { it.estimated1RM } ?: 0,
            allTimeVolume = points.sumOf { it.volume },
            loading = false
        )
    }
}

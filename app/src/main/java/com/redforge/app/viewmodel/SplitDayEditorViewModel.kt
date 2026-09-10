@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.redforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redforge.app.data.local.entities.Exercise
import com.redforge.app.data.local.entities.SplitDay
import com.redforge.app.data.local.entities.SplitDayExercise
import com.redforge.app.data.repository.ExerciseRepository
import com.redforge.app.data.repository.SplitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DayExerciseRow(val entry: SplitDayExercise, val exercise: Exercise)

data class SplitDayEditorUiState(
    val day: SplitDay? = null,
    val rows: List<DayExerciseRow> = emptyList()
)

class SplitDayEditorViewModel(
    private val splitRepository: SplitRepository,
    private val exerciseRepository: ExerciseRepository,
    private val dayId: Long
) : ViewModel() {

    val uiState: StateFlow<SplitDayEditorUiState> = splitRepository.observeDayExercises(dayId)
        .flatMapLatest { entries ->
            if (entries.isEmpty()) {
                flowOf(SplitDayEditorUiState(day = null, rows = emptyList()))
            } else {
                combine(entries.map { entry ->
                    exerciseRepository.observeById(entry.exerciseId).filterNotNull().map { ex -> DayExerciseRow(entry, ex) }
                }) { rows -> SplitDayEditorUiState(rows = rows.sortedBy { it.entry.orderIndex }.toList()) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SplitDayEditorUiState())

    private val _dayName = MutableStateFlow<SplitDay?>(null)
    val dayName: StateFlow<SplitDay?> = _dayName

    init {
        viewModelScope.launch {
            splitRepository.observeDay(dayId).collect { _dayName.value = it }
        }
    }

    fun addExerciseById(exerciseId: Long) {
        viewModelScope.launch { splitRepository.addExerciseToDay(dayId, exerciseId) }
    }

    fun removeExercise(entry: SplitDayExercise) {
        viewModelScope.launch { splitRepository.deleteDayExercise(entry) }
    }

    fun moveExercise(entry: SplitDayExercise, direction: Int) {
        viewModelScope.launch { splitRepository.moveDayExercise(entry, direction) }
    }

    fun toggleSupersetWithNext(entry: SplitDayExercise) {
        viewModelScope.launch { splitRepository.toggleSupersetWithNext(dayId, entry.id) }
    }

    fun updatePrescription(entry: SplitDayExercise, sets: Int, low: Int, high: Int, rest: Int) {
        viewModelScope.launch {
            splitRepository.updateDayExercisePrescription(
                entry.copy(
                    targetSets = sets,
                    targetRepsLow = low,
                    targetRepsHigh = high,
                    targetRestSeconds = rest
                )
            )
        }
    }
}

package com.redforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redforge.app.data.local.entities.Exercise
import com.redforge.app.data.local.entities.SplitDay
import com.redforge.app.data.local.entities.SplitDayExercise
import com.redforge.app.data.repository.ExerciseRepository
import com.redforge.app.data.repository.SplitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DayExerciseRow(val entry: SplitDayExercise, val exercise: Exercise)
data class SplitDayEditorUiState(val day: SplitDay? = null, val rows: List<DayExerciseRow> = emptyList())

@OptIn(ExperimentalCoroutinesApi::class)
class SplitDayEditorViewModel(private val splitRepository: SplitRepository, private val exerciseRepository: ExerciseRepository, private val dayId: Long) : ViewModel() {
    val uiState: StateFlow<SplitDayEditorUiState> = splitRepository.observeDayExercises(dayId).flatMapLatest { entries ->
        if (entries.isEmpty()) flowOf(SplitDayEditorUiState(day = null, rows = emptyList()))
        else combine(entries.map { entry -> exerciseRepository.observeById(entry.exerciseId).filterNotNull().map { ex -> DayExerciseRow(entry, ex) } }) { rows -> SplitDayEditorUiState(rows = rows.sortedBy { it.entry.orderIndex }.toList()) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SplitDayEditorUiState())
    private val _dayName = MutableStateFlow<SplitDay?>(null)
    val dayName: StateFlow<SplitDay?> = _dayName
    init { viewModelScope.launch { _dayName.value = splitRepository.getDay(dayId) } }
    fun addExercise(exercise: Exercise) { viewModelScope.launch { val currentCount = splitRepository.getDayExercisesOnce(dayId).size; splitRepository.saveDayExercise(SplitDayExercise(splitDayId = dayId, exerciseId = exercise.id, orderIndex = currentCount)) } }
    fun addExerciseById(exerciseId: Long) { viewModelScope.launch { val exercise = exerciseRepository.getById(exerciseId) ?: return@launch; addExercise(exercise) } }
    fun updatePrescription(entry: SplitDayExercise, sets: Int, repsLow: Int, repsHigh: Int, restSeconds: Int) { viewModelScope.launch { splitRepository.saveDayExercise(entry.copy(targetSets = sets, targetRepsLow = repsLow, targetRepsHigh = repsHigh, targetRestSeconds = restSeconds)) } }
    fun removeExercise(entry: SplitDayExercise) { viewModelScope.launch { splitRepository.deleteDayExercise(entry) } }
    fun moveExercise(entry: SplitDayExercise, delta: Int) { viewModelScope.launch { val rows = uiState.value.rows; val currentIndex = rows.indexOfFirst { it.entry.id == entry.id }; val targetIndex = currentIndex + delta; if (currentIndex < 0 || targetIndex !in rows.indices) return@launch; val reordered = rows.map { it.entry }.toMutableList(); val moved = reordered.removeAt(currentIndex); reordered.add(targetIndex, moved); splitRepository.replaceDayExercises(dayId, reordered) } }
    fun toggleSupersetWithNext(entry: SplitDayExercise) { viewModelScope.launch { val rows = uiState.value.rows.sortedBy { it.entry.orderIndex }; val index = rows.indexOfFirst { it.entry.id == entry.id }; if (index < 0 || index >= rows.lastIndex) return@launch; val next = rows[index + 1].entry; if (entry.supersetGroup != null && entry.supersetGroup == next.supersetGroup) { val group = entry.supersetGroup; rows.map { it.entry }.filter { it.supersetGroup == group }.forEach { splitRepository.saveDayExercise(it.copy(supersetGroup = null)) } } else { val groupId = entry.supersetGroup ?: next.supersetGroup ?: ((rows.mapNotNull { it.entry.supersetGroup }.maxOrNull() ?: 0) + 1); splitRepository.saveDayExercise(entry.copy(supersetGroup = groupId)); splitRepository.saveDayExercise(next.copy(supersetGroup = groupId)) } } }
}

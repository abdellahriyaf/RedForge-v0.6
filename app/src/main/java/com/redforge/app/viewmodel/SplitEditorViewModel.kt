package com.redforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redforge.app.data.local.entities.Split
import com.redforge.app.data.local.entities.SplitDay
import com.redforge.app.data.repository.SplitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SplitEditorUiState(
    val split: Split? = null,
    val days: List<SplitDay> = emptyList()
)

class SplitEditorViewModel(
    private val repository: SplitRepository,
    private val splitId: Long
) : ViewModel() {

    val uiState: StateFlow<SplitEditorUiState> = repository.observeDays(splitId).map { days ->
        SplitEditorUiState(split = repository.getSplit(splitId), days = days.sortedBy { it.dayOrder })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SplitEditorUiState())

    fun renameSplit(newName: String) {
        viewModelScope.launch {
            val split = repository.getSplit(splitId) ?: return@launch
            repository.saveSplit(split.copy(name = newName))
        }
    }

    fun deleteSplit() {
        viewModelScope.launch {
            val split = repository.getSplit(splitId) ?: return@launch
            repository.deleteSplit(split)
        }
    }

    fun setDeloadCycle(enabled: Boolean) {
        viewModelScope.launch {
            val split = repository.getSplit(splitId) ?: return@launch
            repository.saveSplit(split.copy(isDeloadCycle = enabled))
        }
    }

    fun renameDay(day: SplitDay, newName: String) {
        viewModelScope.launch { repository.saveDay(day.copy(name = newName)) }
    }

    fun addDay(name: String) {
        viewModelScope.launch {
            val nextOrder = (uiState.value.days.maxOfOrNull { it.dayOrder } ?: 0) + 1
            repository.saveDay(SplitDay(splitId = splitId, name = name, dayOrder = nextOrder))
            syncDayCount()
        }
    }

    fun addRestDay() {
        viewModelScope.launch {
            val nextOrder = (uiState.value.days.maxOfOrNull { it.dayOrder } ?: 0) + 1
            repository.saveDay(SplitDay(splitId = splitId, name = "Rest Day", dayOrder = nextOrder, isRestDay = true))
            syncDayCount()
        }
    }

    fun deleteDay(day: SplitDay) {
        viewModelScope.launch {
            repository.deleteDay(day)
            syncDayCount()
        }
    }

    /** Moves a day up/down in the cycle order — this is the "flexible reordering" the user asked for. */
    fun moveDay(day: SplitDay, delta: Int) {
        viewModelScope.launch {
            val ordered = uiState.value.days
            val currentIndex = ordered.indexOfFirst { it.id == day.id }
            val targetIndex = currentIndex + delta
            if (currentIndex < 0 || targetIndex !in ordered.indices) return@launch
            val a = ordered[currentIndex]
            val b = ordered[targetIndex]
            repository.saveDay(a.copy(dayOrder = b.dayOrder))
            repository.saveDay(b.copy(dayOrder = a.dayOrder))
        }
    }

    private suspend fun syncDayCount() {
        val split = repository.getSplit(splitId) ?: return
        val count = repository.observeDays(splitId).first().size
        repository.saveSplit(split.copy(daysPerCycle = count))
    }
}

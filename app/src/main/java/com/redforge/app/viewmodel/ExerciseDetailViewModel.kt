package com.redforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redforge.app.data.local.entities.Exercise
import com.redforge.app.data.repository.ExerciseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ExerciseDetailViewModel(
    repository: ExerciseRepository,
    exerciseId: Long
) : ViewModel() {
    val exercise: StateFlow<Exercise?> = repository.observeById(exerciseId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

package com.redforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redforge.app.data.local.entities.BodyMeasurement
import com.redforge.app.data.repository.ProgressRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BodyMeasurementViewModel(private val repository: ProgressRepository) : ViewModel() {
    val measurements: StateFlow<List<BodyMeasurement>> = repository.observeAllMeasurements()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun save(measurement: BodyMeasurement) {
        viewModelScope.launch { repository.saveMeasurement(measurement) }
    }

    fun delete(measurement: BodyMeasurement) {
        viewModelScope.launch { repository.deleteMeasurement(measurement) }
    }
}

package com.redforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redforge.app.data.datastore.ForgeSettings
import com.redforge.app.data.datastore.SettingsDataStore
import com.redforge.app.data.datastore.WeightUnit
import com.redforge.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsDataStore,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    val state: StateFlow<ForgeSettings> = settings.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ForgeSettings())

    fun setWeightUnit(unit: WeightUnit) = viewModelScope.launch {
        val current = state.value.weightUnit
        if (current != unit) {
            val factor = if (current == WeightUnit.KG && unit == WeightUnit.LB) {
                2.2046226218
            } else {
                0.45359237
            }
            // Stored values stay in the user-selected unit so historical data
            // keeps the same meaning after a kg/lb switch.
            workoutRepository.convertAllSetWeights(factor)
            settings.setWeightUnit(unit)
        }
    }

    fun setDefaultRest(seconds: Int) = viewModelScope.launch { settings.setDefaultRestSeconds(seconds) }
    fun setDarkForced(forced: Boolean) = viewModelScope.launch { settings.setDarkThemeForced(forced) }
    fun setTimerSound(enabled: Boolean) = viewModelScope.launch { settings.setTimerSoundEnabled(enabled) }
    fun setTimerVibration(enabled: Boolean) = viewModelScope.launch { settings.setTimerVibrationEnabled(enabled) }
}

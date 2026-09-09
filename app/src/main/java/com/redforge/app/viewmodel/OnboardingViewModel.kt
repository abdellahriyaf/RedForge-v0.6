package com.redforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redforge.app.data.datastore.ForgeSettings
import com.redforge.app.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(private val settings: SettingsDataStore) : ViewModel() {

    val forgeSettings: StateFlow<ForgeSettings> = settings.settingsFlow.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.Eagerly,
        ForgeSettings()
    )

    fun acceptPrivacyPolicy() {
        viewModelScope.launch { settings.setPrivacyAccepted(true) }
    }

    fun completeTutorial() {
        viewModelScope.launch { settings.setOnboardingSeen(true) }
    }
}

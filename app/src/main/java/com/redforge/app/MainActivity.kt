package com.redforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.redforge.app.navigation.RedForgeApp
import com.redforge.app.ui.theme.RedForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as RedForgeApplication
        setContent {
            val settings by app.settingsDataStore.settingsFlow.collectAsState(initial = com.redforge.app.data.datastore.ForgeSettings())
            RedForgeTheme(forceDark = settings.darkThemeForced) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RedForgeApp(openWorkoutOnLaunch = intent.getBooleanExtra("start_workout", false))
                }
            }
        }
    }
}

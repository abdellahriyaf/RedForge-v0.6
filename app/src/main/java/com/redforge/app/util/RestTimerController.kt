package com.redforge.app.util

import android.content.Context
import android.content.Intent
import android.os.Build
import com.redforge.app.service.RestTimerService
import kotlinx.coroutines.flow.StateFlow

/** Small facade so Composables/ViewModels don't talk to Intents directly. */
class RestTimerController(private val context: Context) {

    val state: StateFlow<RestTimerService.Companion.TimerUiState> = RestTimerService.state

    // Only ACTION_START begins genuinely new foreground work, so only it is allowed
    // to use startForegroundService(). That API requires the service to call
    // startForeground() within a few seconds or the whole app process gets killed
    // with a fatal ForegroundServiceDidNotStartInTimeException. PAUSE/RESUME/ADD_15/
    // SKIP/STOP are just control messages to a service that (if running) is already
    // in the foreground — sending those via startForegroundService() crashed the app
    // whenever the service didn't call startForeground() again for them (e.g. STOP,
    // which just stops itself). Plain startService() delivers the same Intent without
    // that contract, and is safe here because these calls only ever happen while the
    // app itself is in the foreground.
    fun start(durationSeconds: Int) = send(RestTimerService.ACTION_START, requiresForeground = true) {
        putExtra(RestTimerService.EXTRA_DURATION_SECONDS, durationSeconds)
    }

    fun pause() = send(RestTimerService.ACTION_PAUSE)
    fun resume() = send(RestTimerService.ACTION_RESUME)
    fun addFifteenSeconds() = send(RestTimerService.ACTION_ADD_15)
    fun skip() = send(RestTimerService.ACTION_SKIP)
    fun stop() = send(RestTimerService.ACTION_STOP)

    private fun send(action: String, requiresForeground: Boolean = false, configure: Intent.() -> Unit = {}) {
        val intent = Intent(context, RestTimerService::class.java).setAction(action).apply(configure)
        if (requiresForeground && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}

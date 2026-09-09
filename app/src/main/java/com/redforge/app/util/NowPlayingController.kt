package com.redforge.app.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.provider.Settings
import com.redforge.app.service.RedForgeNotificationListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val SPOTIFY_PACKAGE = "com.spotify.music"

/**
 * Drives the Home screen's "Now Playing" media card using Android's system
 * MediaSession API — this gives real play/pause/skip control over whatever
 * app is currently playing music (Spotify, YouTube Music, etc.), without
 * needing that app's own SDK. It requires the user to grant RedForge
 * "Notification access" once (the standard Android permission this API is
 * gated behind) — see [hasAccess] and [openAccessSettings].
 *
 * A process-wide singleton (like [com.redforge.app.service.RestTimerService]'s
 * companion state) since it just mirrors the OS's live session — no need to
 * scope it per-screen.
 */
object NowPlayingController {

    data class NowPlayingUiState(
        val accessGranted: Boolean = false,
        val title: String? = null,
        val artist: String? = null,
        val appLabel: String? = null,
        val isPlaying: Boolean = false
    )

    private val _state = MutableStateFlow(NowPlayingUiState())
    val state: StateFlow<NowPlayingUiState> = _state.asStateFlow()

    private var appContext: Context? = null
    private var activeController: MediaController? = null
    private var activeCallback: MediaController.Callback? = null
    private var sessionsListener: MediaSessionManager.OnActiveSessionsChangedListener? = null

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        refresh()
    }

    fun hasAccess(context: Context): Boolean {
        val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: ""
        return enabled.contains(context.packageName)
    }

    fun openAccessSettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** Call when the Home screen resumes — e.g. after coming back from the system settings screen. */
    fun refresh() {
        val context = appContext ?: return
        if (!hasAccess(context)) {
            teardown()
            _state.value = NowPlayingUiState(accessGranted = false)
            return
        }
        try {
            val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val componentName = ComponentName(context, RedForgeNotificationListener::class.java)

            if (sessionsListener == null) {
                val listener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
                    bindTo(pickPreferred(controllers))
                }
                manager.addOnActiveSessionsChangedListener(listener, componentName)
                sessionsListener = listener
            }
            bindTo(pickPreferred(manager.getActiveSessions(componentName)))
        } catch (e: SecurityException) {
            _state.value = NowPlayingUiState(accessGranted = false)
        }
    }

    /** Spotify wins by default when more than one app has an active session — falls back to whichever is actually playing, then whatever's first. */
    private fun pickPreferred(controllers: List<MediaController>?): MediaController? {
        if (controllers.isNullOrEmpty()) return null
        return controllers.firstOrNull { it.packageName == SPOTIFY_PACKAGE }
            ?: controllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: controllers.firstOrNull()
    }

    private fun bindTo(controller: MediaController?) {
        activeController?.let { old -> activeCallback?.let { cb -> old.unregisterCallback(cb) } }
        activeController = controller
        activeCallback = null

        if (controller == null) {
            _state.value = NowPlayingUiState(accessGranted = true)
            return
        }

        updateFromController(controller)
        val callback = object : MediaController.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackState?) = updateFromController(controller)
            override fun onMetadataChanged(metadata: MediaMetadata?) = updateFromController(controller)
            override fun onSessionDestroyed() {
                if (activeController == controller) refresh()
            }
        }
        controller.registerCallback(callback)
        activeCallback = callback
    }

    private fun updateFromController(controller: MediaController) {
        val metadata = controller.metadata
        val playback = controller.playbackState
        _state.value = NowPlayingUiState(
            accessGranted = true,
            title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST),
            appLabel = appLabelFor(controller.packageName),
            isPlaying = playback?.state == PlaybackState.STATE_PLAYING
        )
    }

    private fun appLabelFor(packageName: String): String? {
        val context = appContext ?: return null
        return try {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: Exception) {
            null
        }
    }

    fun playPause() {
        val controller = activeController ?: return
        if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
            controller.transportControls.pause()
        } else {
            controller.transportControls.play()
        }
    }

    fun skipNext() {
        activeController?.transportControls?.skipToNext()
    }

    fun skipPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }

    private fun teardown() {
        activeController?.let { old -> activeCallback?.let { cb -> old.unregisterCallback(cb) } }
        activeController = null
        activeCallback = null
        val context = appContext
        val listener = sessionsListener
        if (context != null && listener != null) {
            try {
                val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
                manager.removeOnActiveSessionsChangedListener(listener)
            } catch (e: SecurityException) {
                // Access was already revoked — nothing to clean up on the OS side.
            }
        }
        sessionsListener = null
    }
}

package com.redforge.app.service

import android.service.notification.NotificationListenerService

/**
 * We never read, store, or transmit notification content — this service's
 * only purpose is to satisfy the Android requirement for reading the
 * device's active [android.media.session.MediaSession]s, which is how the
 * Home screen's media card gets a real play/pause/skip control over
 * whatever music app (Spotify, YouTube Music, etc.) is currently playing.
 *
 * Everything this reads (track title/artist, playback state) comes
 * straight from the system MediaSession API and never leaves the device —
 * same local-only guarantee as the rest of RedForge.
 */
class RedForgeNotificationListener : NotificationListenerService()

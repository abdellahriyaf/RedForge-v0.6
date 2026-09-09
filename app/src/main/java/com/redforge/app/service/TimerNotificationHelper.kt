package com.redforge.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Locale
import androidx.core.app.NotificationCompat
import com.redforge.app.MainActivity

object TimerNotificationHelper {
    const val CHANNEL_ID = "rest_timer_channel"
    const val NOTIFICATION_ID = 4201

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Rest Timer",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Shows the countdown between sets while you're training"
                    setShowBadge(false)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun build(context: Context, secondsRemaining: Int, isPaused: Boolean): android.app.Notification {
        ensureChannel(context)

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        fun actionIntent(action: String): PendingIntent {
            val intent = Intent(context, RestTimerService::class.java).setAction(action)
            return PendingIntent.getService(
                context, action.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining % 60
        val timeText = String.format(Locale.US, "%d:%02d", minutes, seconds)

        val playPauseAction = if (isPaused) {
            NotificationCompat.Action(0, "Resume", actionIntent(RestTimerService.ACTION_RESUME))
        } else {
            NotificationCompat.Action(0, "Pause", actionIntent(RestTimerService.ACTION_PAUSE))
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play) // replace with @drawable/ic_forge_flame in Android Studio asset step
            .setContentTitle("Rest — $timeText")
            .setContentText(if (isPaused) "Paused" else "Tap to return to your workout")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .addAction(playPauseAction)
            .addAction(0, "+15s", actionIntent(RestTimerService.ACTION_ADD_15))
            .addAction(0, "Skip", actionIntent(RestTimerService.ACTION_SKIP))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}

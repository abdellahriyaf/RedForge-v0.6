package com.redforge.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * Lightweight Spotify integration: since the app requests no INTERNET
 * permission and takes a hard local-only stance, we don't bundle the full
 * Spotify App Remote SDK by default (it needs its own network use inside
 * the Spotify app, and a manual SDK download — see README.md "Optional:
 * Spotify integration" for wiring it up with real playback controls).
 *
 * Out of the box, RedForge offers a lightweight, permission-free bridge:
 * jump straight into the Spotify app (or its Play Store listing if it's
 * not installed) so the user can start music alongside their workout with
 * one tap from the workout screen.
 */
object SpotifyLauncher {
    private const val SPOTIFY_PACKAGE = "com.spotify.music"

    fun isSpotifyInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(SPOTIFY_PACKAGE, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    fun openSpotify(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(SPOTIFY_PACKAGE)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            val marketIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$SPOTIFY_PACKAGE"))
            context.startActivity(marketIntent)
        }
    }
}

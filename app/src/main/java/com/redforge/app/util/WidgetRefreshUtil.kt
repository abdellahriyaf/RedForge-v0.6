package com.redforge.app.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.redforge.app.widget.RedForgeWidgetReceiver

/** Requests an immediate refresh for any RedForge widgets currently placed on the launcher. */
object WidgetRefreshUtil {
    fun request(context: Context) {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext)
        val provider = ComponentName(appContext, RedForgeWidgetReceiver::class.java)
        val ids = manager.getAppWidgetIds(provider)
        if (ids.isEmpty()) return

        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            this.component = provider
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            setPackage(appContext.packageName)
        }
        appContext.sendBroadcast(intent)
    }
}

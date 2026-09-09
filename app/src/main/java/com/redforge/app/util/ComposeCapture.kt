package com.redforge.app.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Dp

/**
 * Renders a Composable off-screen at a fixed pixel size and captures it to
 * a [Bitmap] — the mechanism behind "share as an image" for workout/streak
 * result cards. Temporarily attaches a [ComposeView] to the activity's root,
 * measures/lays it out at an exact size, draws it to a canvas-backed
 * bitmap, then detaches it — the user never sees the intermediate view.
 */
object ComposeCapture {

    fun captureToBitmap(
        activity: Activity,
        widthDp: Dp,
        heightDp: Dp,
        content: @Composable () -> Unit,
        onCaptured: (Bitmap) -> Unit
    ) {
        val density = activity.resources.displayMetrics.density
        val widthPx = (widthDp.value * density).toInt()
        val heightPx = (heightDp.value * density).toInt()

        val composeView = ComposeView(activity).apply { setContent(content) }
        val root = activity.window.decorView as ViewGroup
        root.addView(composeView, ViewGroup.LayoutParams(widthPx, heightPx))

        composeView.post {
            composeView.measure(
                View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
            )
            composeView.layout(0, 0, widthPx, heightPx)

            val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            composeView.draw(canvas)

            root.removeView(composeView)
            onCaptured(bitmap)
        }
    }
}

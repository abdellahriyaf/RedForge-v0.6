package com.redforge.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/** Saves a captured result-card bitmap to the app's cache and fires a standard share sheet. */
object ShareImageUtil {
    fun shareBitmap(context: Context, bitmap: Bitmap, fileName: String = "redforge_result.png") {
        val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(shareDir, fileName)
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share your RedForge result"))
    }
}

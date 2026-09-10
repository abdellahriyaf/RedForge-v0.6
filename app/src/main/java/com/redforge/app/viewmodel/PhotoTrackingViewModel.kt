package com.redforge.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redforge.app.data.local.entities.PhotoAngle
import com.redforge.app.data.local.entities.ProgressPhoto
import com.redforge.app.data.repository.ProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PhotoTrackingViewModel(
    private val repository: ProgressRepository,
    private val appContext: Context
) : ViewModel() {

    val photos: StateFlow<List<ProgressPhoto>> = repository.observeAllPhotos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Copies the picked/captured image into the app's private files dir.
     * All filesystem work is performed on Dispatchers.IO.
     */
    fun savePhoto(sourceUri: android.net.Uri, angle: PhotoAngle, note: String): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            val dir = File(appContext.filesDir, "progress_photos").apply { mkdirs() }
            val destFile = File(dir, "photo_${System.currentTimeMillis()}.jpg")
            appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return@launch
            repository.addPhoto(
                ProgressPhoto(
                    filePath = destFile.absolutePath,
                    angle = angle,
                    note = note
                )
            )
        }
    }

    fun deletePhoto(photo: ProgressPhoto) {
        viewModelScope.launch(Dispatchers.IO) {
            File(photo.filePath).delete()
            repository.deletePhoto(photo)
        }
    }
}

package com.redforge.app.data.repository

import com.redforge.app.data.local.dao.ProgressDao
import com.redforge.app.data.local.entities.BodyMeasurement
import com.redforge.app.data.local.entities.ProgressPhoto
import kotlinx.coroutines.flow.Flow

class ProgressRepository(private val dao: ProgressDao) {
    fun observeAllPhotos(): Flow<List<ProgressPhoto>> = dao.observeAllPhotos()
    fun observePhotosByAngle(angle: String): Flow<List<ProgressPhoto>> = dao.observePhotosByAngle(angle)
    suspend fun addPhoto(photo: ProgressPhoto): Long = dao.insertPhoto(photo)
    suspend fun deletePhoto(photo: ProgressPhoto) = dao.deletePhoto(photo)

    fun observeAllMeasurements(): Flow<List<BodyMeasurement>> = dao.observeAllMeasurements()
    fun observeLatestMeasurement(): Flow<BodyMeasurement?> = dao.observeLatestMeasurement()
    suspend fun saveMeasurement(m: BodyMeasurement): Long = dao.upsertMeasurement(m)
    suspend fun deleteMeasurement(m: BodyMeasurement) = dao.deleteMeasurement(m)
}

package com.redforge.app.data.local.dao

import androidx.room.*
import com.redforge.app.data.local.entities.BodyMeasurement
import com.redforge.app.data.local.entities.ProgressPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Query("SELECT * FROM progress_photos ORDER BY takenAt DESC")
    fun observeAllPhotos(): Flow<List<ProgressPhoto>>

    @Query("SELECT * FROM progress_photos WHERE angle = :angle ORDER BY takenAt DESC")
    fun observePhotosByAngle(angle: String): Flow<List<ProgressPhoto>>

    @Insert
    suspend fun insertPhoto(photo: ProgressPhoto): Long

    @Delete
    suspend fun deletePhoto(photo: ProgressPhoto)

    @Query("SELECT * FROM body_measurements ORDER BY date DESC")
    fun observeAllMeasurements(): Flow<List<BodyMeasurement>>

    @Query("SELECT * FROM body_measurements ORDER BY date DESC LIMIT 1")
    fun observeLatestMeasurement(): Flow<BodyMeasurement?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeasurement(measurement: BodyMeasurement): Long

    @Delete
    suspend fun deleteMeasurement(measurement: BodyMeasurement)
}

package com.redforge.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.redforge.app.data.local.db.RedForgeDatabase
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Manual, user-initiated local backup/restore.
 *
 * The backup contains the Room database, DataStore preferences and progress
 * photos. Nothing is uploaded automatically by RedForge.
 */
object DataBackupUtil {

    private const val BACKUP_FORMAT_VERSION = 1
    private const val DB_ENTRY = "redforge.db"
    private const val PREFS_ENTRY = "redforge_settings.preferences_pb"
    private const val PHOTOS_ENTRY_PREFIX = "progress_photos/"
    private const val MARKER_ENTRY = "redforge_backup_marker.txt"
    private const val MAX_ENTRY_BYTES = 100L * 1024L * 1024L
    private const val MAX_BACKUP_UNCOMPRESSED_BYTES = 250L * 1024L * 1024L

    fun exportBackup(context: Context): Uri? = try {
        val db = RedForgeDatabase.getInstance(context)
        if (!db.isOpen) return null

        db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(TRUNCATE)").use { }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val exportDir = File(context.cacheDir, "share").apply { mkdirs() }
        val zipFile = File(exportDir, "redforge_backup_$timestamp.zip")
        val dbFile = context.getDatabasePath("redforge.db")
        if (!dbFile.isFile) return null

        ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
            writeTextEntry(
                zip,
                MARKER_ENTRY,
                "RedForge backup|format=$BACKUP_FORMAT_VERSION|created=$timestamp"
            )
            addFileToZip(zip, dbFile, DB_ENTRY)

            val prefsFile = File(
                context.filesDir.parentFile,
                "datastore/redforge_settings.preferences_pb"
            )
            if (prefsFile.isFile) addFileToZip(zip, prefsFile, PREFS_ENTRY)

            val photosDir = File(context.filesDir, "progress_photos")
            photosDir.listFiles()?.filter { it.isFile }?.forEach { photo ->
                val safeName = photo.name.replace("/", "_").replace("\\", "_")
                addFileToZip(zip, photo, PHOTOS_ENTRY_PREFIX + safeName)
            }
        }

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            zipFile
        )
    } catch (_: Exception) {
        null
    }

    fun shareBackup(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Save your RedForge backup"))
    }

    fun isValidBackup(context: Context, uri: Uri): Boolean = try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input).use { zip ->
                var total = 0L
                var markerFound = false
                var databaseFound = false
                var entry = zip.nextEntry

                while (entry != null) {
                    validateEntryName(entry.name)
                    when (entry.name) {
                        MARKER_ENTRY -> {
                            val bytes = readEntryLimited(zip, MAX_ENTRY_BYTES)
                            if (!isSupportedMarker(bytes.toString(Charsets.UTF_8))) return@use false
                            markerFound = true
                            total += bytes.size
                        }
                        DB_ENTRY -> {
                            total += skipEntry(zip)
                            databaseFound = true
                        }
                        else -> total += skipEntry(zip)
                    }
                    if (total > MAX_BACKUP_UNCOMPRESSED_BYTES) return@use false
                    entry = zip.nextEntry
                }
                markerFound && databaseFound
            }
        } ?: false
    } catch (_: Exception) {
        false
    }

    /**
     * Extracts to a private staging directory, validates the staged database,
     * then replaces the live files. The original files are kept in a rollback
     * directory until the replacement succeeds.
     */
    fun importBackup(context: Context, uri: Uri): Boolean {
        val stagingRoot = File(context.cacheDir, "restore_${UUID.randomUUID()}")
        val rollbackRoot = File(context.cacheDir, "rollback_${UUID.randomUUID()}")
        val stagedDb = File(stagingRoot, DB_ENTRY)
        val stagedPrefs = File(stagingRoot, PREFS_ENTRY)
        val stagedPhotos = File(stagingRoot, PHOTOS_ENTRY_PREFIX)
        val currentDb = context.getDatabasePath("redforge.db")
        val currentPrefs = File(context.filesDir.parentFile, "datastore/redforge_settings.preferences_pb")
        val currentPhotos = File(context.filesDir, "progress_photos")
        val rollbackDb = File(rollbackRoot, DB_ENTRY)
        val rollbackPrefs = File(rollbackRoot, PREFS_ENTRY)
        val rollbackPhotos = File(rollbackRoot, PHOTOS_ENTRY_PREFIX)

        var committed = false

        return try {
            stagingRoot.mkdirs()
            stagedPhotos.mkdirs()

            var total = 0L
            var markerFound = false
            var databaseFound = false
            var preferencesFound = false

            context.contentResolver.openInputStream(uri)?.use { input ->
                ZipInputStream(input).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        validateEntryName(entry.name)
                        when {
                            entry.name == MARKER_ENTRY -> {
                                val bytes = readEntryLimited(zip, MAX_ENTRY_BYTES)
                                if (!isSupportedMarker(bytes.toString(Charsets.UTF_8))) {
                                    throw IllegalArgumentException("Unsupported backup format")
                                }
                                markerFound = true
                                total += bytes.size
                            }
                            entry.name == DB_ENTRY -> {
                                total += writeEntryLimited(zip, stagedDb)
                                databaseFound = true
                            }
                            entry.name == PREFS_ENTRY -> {
                                total += writeEntryLimited(zip, stagedPrefs)
                                preferencesFound = true
                            }
                            entry.name.startsWith(PHOTOS_ENTRY_PREFIX) && !entry.isDirectory -> {
                                val fileName = entry.name.removePrefix(PHOTOS_ENTRY_PREFIX)
                                if (fileName.isBlank() || fileName.contains("/") || fileName.contains("\\")) {
                                    throw IllegalArgumentException("Unsafe photo path")
                                }
                                total += writeEntryLimited(zip, File(stagedPhotos, fileName))
                            }
                            else -> skipEntry(zip)
                        }
                        if (total > MAX_BACKUP_UNCOMPRESSED_BYTES) {
                            throw IllegalArgumentException("Backup too large")
                        }
                        entry = zip.nextEntry
                    }
                }
            } ?: return false

            if (!markerFound || !databaseFound || !stagedDb.isFile) return false
            validateSQLiteDatabase(stagedDb)

            rollbackRoot.mkdirs()
            if (currentDb.isFile) copyFile(currentDb, rollbackDb)
            if (currentPrefs.isFile) copyFile(currentPrefs, rollbackPrefs)
            if (currentPhotos.isDirectory) copyDirectory(currentPhotos, rollbackPhotos)

            RedForgeDatabase.closeInstance()
            File(currentDb.path + "-wal").delete()
            File(currentDb.path + "-shm").delete()
            currentDb.parentFile?.mkdirs()
            if (currentDb.exists() && !currentDb.delete()) throw IllegalStateException("Could not replace database")
            copyFile(stagedDb, currentDb)

            if (preferencesFound) {
                currentPrefs.parentFile?.mkdirs()
                copyFile(stagedPrefs, currentPrefs)
            } else {
                currentPrefs.delete()
            }

            if (currentPhotos.exists()) currentPhotos.deleteRecursively()
            if (stagedPhotos.isDirectory) copyDirectory(stagedPhotos, currentPhotos)

            committed = true
            true
        } catch (_: Exception) {
            if (!committed) {
                try {
                    RedForgeDatabase.closeInstance()
                    File(currentDb.path + "-wal").delete()
                    File(currentDb.path + "-shm").delete()
                    if (rollbackDb.isFile) {
                        currentDb.parentFile?.mkdirs()
                        if (currentDb.exists()) currentDb.delete()
                        copyFile(rollbackDb, currentDb)
                    }
                    if (rollbackPrefs.isFile) {
                        currentPrefs.parentFile?.mkdirs()
                        copyFile(rollbackPrefs, currentPrefs)
                    }
                    if (rollbackPhotos.isDirectory) {
                        currentPhotos.deleteRecursively()
                        copyDirectory(rollbackPhotos, currentPhotos)
                    }
                } catch (_: Exception) {
                    // Keep the original failure result; there is no safe further action here.
                }
            }
            false
        } finally {
            stagingRoot.deleteRecursively()
            rollbackRoot.deleteRecursively()
        }
    }

    fun restartApp(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        context.startActivity(Intent.makeRestartActivityTask(launchIntent.component))
        Runtime.getRuntime().exit(0)
    }

    private fun isSupportedMarker(marker: String): Boolean =
        marker.startsWith("RedForge backup|format=$BACKUP_FORMAT_VERSION|") ||
            marker.startsWith("RedForge backup — created ")

    private fun validateEntryName(name: String) {
        if (
            name.isBlank() || name.startsWith("/") || name.startsWith("\\") ||
            name.contains("..") || name.contains("\\")
        ) throw IllegalArgumentException("Unsafe archive entry")

        val allowed = name == DB_ENTRY ||
            name == PREFS_ENTRY ||
            name == MARKER_ENTRY ||
            name.startsWith(PHOTOS_ENTRY_PREFIX)
        if (!allowed) throw IllegalArgumentException("Unknown archive entry")
    }

    private fun validateSQLiteDatabase(file: File) {
        if (!file.isFile || file.length() < 100) throw IllegalArgumentException("Invalid database")
        val header = ByteArray(16)
        FileInputStream(file).use { input ->
            var offset = 0
            while (offset < header.size) {
                val read = input.read(header, offset, header.size - offset)
                if (read < 0) break
                offset += read
            }
        }
        val signature = String(header, Charsets.US_ASCII)
        if (!signature.startsWith("SQLite format 3\u0000")) {
            throw IllegalArgumentException("Invalid SQLite database")
        }
    }

    private fun copyFile(source: File, destination: File) {
        destination.parentFile?.mkdirs()
        FileInputStream(source).use { input ->
            FileOutputStream(destination).use { output -> input.copyTo(output) }
        }
    }

    private fun copyDirectory(source: File, destination: File) {
        destination.mkdirs()
        source.listFiles()?.forEach { file ->
            val target = File(destination, file.name)
            if (file.isDirectory) copyDirectory(file, target) else copyFile(file, target)
        }
    }

    private fun writeTextEntry(zip: ZipOutputStream, entryName: String, text: String) {
        zip.putNextEntry(ZipEntry(entryName))
        zip.write(text.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun addFileToZip(zip: ZipOutputStream, file: File, entryName: String) {
        if (!file.isFile) return
        zip.putNextEntry(ZipEntry(entryName))
        FileInputStream(file).use { input -> input.copyTo(zip) }
        zip.closeEntry()
    }

    private fun readEntryLimited(zip: ZipInputStream, limit: Long): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = zip.read(buffer)
            if (read < 0) break
            total += read
            if (total > limit) throw IllegalArgumentException("Archive entry too large")
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }

    private fun writeEntryLimited(zip: ZipInputStream, destination: File): Long {
        destination.parentFile?.mkdirs()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        FileOutputStream(destination).use { output ->
            while (true) {
                val read = zip.read(buffer)
                if (read < 0) break
                total += read
                if (total > MAX_ENTRY_BYTES) throw IllegalArgumentException("Archive entry too large")
                output.write(buffer, 0, read)
            }
        }
        return total
    }

    private fun skipEntry(zip: ZipInputStream): Long {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = zip.read(buffer)
            if (read < 0) break
            total += read
            if (total > MAX_ENTRY_BYTES) throw IllegalArgumentException("Archive entry too large")
        }
        return total
    }
}

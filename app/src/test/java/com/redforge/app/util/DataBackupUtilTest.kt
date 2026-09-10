package com.redforge.app.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class DataBackupUtilTest {

    @Test
    fun supported_marker_is_accepted_and_unrelated_marker_is_rejected() {
        assertTrue(invokeBoolean("isSupportedMarker", "RedForge backup|format=1|created=20260910_120000"))
        assertTrue(invokeBoolean("isSupportedMarker", "RedForge backup — created 20260910"))
        assertFalse(invokeBoolean("isSupportedMarker", "not a RedForge backup"))
    }

    @Test
    fun archive_path_validation_rejects_traversal_and_unknown_entries() {
        assertValidationAccepts("redforge.db")
        assertValidationAccepts("redforge_settings.preferences_pb")
        assertValidationAccepts("redforge_backup_marker.txt")
        assertValidationAccepts("progress_photos/front.jpg")

        assertValidationRejects("../redforge.db")
        assertValidationRejects("progress_photos/../secret.jpg")
        assertValidationRejects("/absolute/path")
        assertValidationRejects("unexpected.bin")
    }

    @Test
    fun limited_zip_reader_reads_expected_bytes() {
        val source = zipOf("redforge_backup_marker.txt", "RedForge backup|format=1|")
        ZipInputStream(ByteArrayInputStream(source.toByteArray())).use { zip ->
            zip.nextEntry
            val bytes = invokeByteArray("readEntryLimited", zip, 1024L)
            assertArrayEquals("RedForge backup|format=1|".toByteArray(), bytes)
        }
    }

    @Test
    fun limited_zip_reader_rejects_oversized_entries() {
        val source = zipOf("redforge_backup_marker.txt", "1234567890")
        ZipInputStream(ByteArrayInputStream(source.toByteArray())).use { zip ->
            zip.nextEntry
            try {
                invokeByteArray("readEntryLimited", zip, 5L)
                throw AssertionError("Expected oversized entry to be rejected")
            } catch (ex: InvocationTargetException) {
                assertTrue(ex.targetException is IllegalArgumentException)
            }
        }
    }

    private fun assertValidationAccepts(name: String) {
        invokeVoid("validateEntryName", name)
    }

    private fun assertValidationRejects(name: String) {
        try {
            invokeVoid("validateEntryName", name)
            throw AssertionError("Expected unsafe entry to be rejected: $name")
        } catch (ex: InvocationTargetException) {
            assertTrue(ex.targetException is IllegalArgumentException)
        }
    }

    private fun zipOf(name: String, content: String): ByteArrayOutputStream {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry(name))
            zip.write(content.toByteArray())
            zip.closeEntry()
        }
        return output
    }

    private fun privateMethod(name: String, vararg parameterTypes: Class<*>): Method =
        DataBackupUtil::class.java.getDeclaredMethod(name, *parameterTypes).apply {
            isAccessible = true
        }

    private fun invokeBoolean(name: String, argument: String): Boolean =
        privateMethod(name, String::class.java).invoke(DataBackupUtil, argument) as Boolean

    private fun invokeVoid(name: String, argument: String) {
        privateMethod(name, String::class.java).invoke(DataBackupUtil, argument)
    }

    private fun invokeByteArray(name: String, zip: ZipInputStream, limit: Long): ByteArray =
        privateMethod(name, ZipInputStream::class.java, Long::class.javaPrimitiveType!!)
            .invoke(DataBackupUtil, zip, limit) as ByteArray
}

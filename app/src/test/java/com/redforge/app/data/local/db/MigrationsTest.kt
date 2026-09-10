package com.redforge.app.data.local.db

import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class MigrationsTest {

    @Test
    fun all_migrations_form_a_contiguous_non_destructive_chain() {
        assertEquals(listOf(1 to 2, 2 to 3, 3 to 4), ALL_MIGRATIONS.map { it.startVersion to it.endVersion })
    }

    @Test
    fun migration_1_2_adds_only_the_expected_columns() {
        val statements = captureStatements { MIGRATION_1_2.migrate(it) }

        assertEquals(3, statements.size)
        assertTrue(statements[0].contains("ADD COLUMN isPersonalRecord"))
        assertTrue(statements[1].contains("ADD COLUMN isDeloadCycle"))
        assertTrue(statements[2].contains("ADD COLUMN supersetGroup"))
        assertTrue(statements.all { it.startsWith("ALTER TABLE") })
    }

    @Test
    fun migration_2_3_adds_the_structured_exercise_metadata() {
        val statements = captureStatements { MIGRATION_2_3.migrate(it) }

        assertEquals(12, statements.size)
        assertTrue(statements.any { it.contains("ADD COLUMN aliases") })
        assertTrue(statements.any { it.contains("ADD COLUMN primaryMuscles") })
        assertTrue(statements.any { it.contains("ADD COLUMN secondaryMuscles") })
        assertTrue(statements.any { it.contains("ADD COLUMN movementPattern") })
        assertTrue(statements.any { it.contains("ADD COLUMN demoAsset") })
        assertTrue(statements.any { it.contains("ADD COLUMN sourceLicense") })
        assertTrue(statements.any { it.contains("ADD COLUMN sourceAttribution") })
    }

    @Test
    fun migration_3_4_contains_only_idempotent_indexes() {
        val statements = captureStatements { MIGRATION_3_4.migrate(it) }

        assertEquals(12, statements.size)
        assertTrue(statements.all { it.startsWith("CREATE INDEX IF NOT EXISTS") })
        assertTrue(statements.any { it.contains("index_set_entries_exerciseId_loggedAt") })
        assertTrue(statements.any { it.contains("index_workout_sessions_completed_startedAt") })
        assertTrue(statements.any { it.contains("index_progress_photos_takenAt") })
        assertTrue(statements.any { it.contains("index_body_measurements_date") })
    }

    private fun captureStatements(block: (SupportSQLiteDatabase) -> Unit): List<String> {
        val statements = mutableListOf<String>()
        val database = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && !args.isNullOrEmpty()) {
                statements += args[0] as String
            }
            null
        } as SupportSQLiteDatabase

        block(database)
        return statements
    }
}

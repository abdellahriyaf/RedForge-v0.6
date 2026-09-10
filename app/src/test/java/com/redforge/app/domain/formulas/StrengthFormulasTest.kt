package com.redforge.app.domain.formulas

import com.redforge.app.data.local.entities.SetEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthFormulasTest {

    @Test
    fun epley_handles_one_rep_and_zero_reps() {
        assertEquals(100.0, StrengthFormulas.epley1RM(100.0, 1), 0.0001)
        assertEquals(0.0, StrengthFormulas.epley1RM(100.0, 0), 0.0001)
    }

    @Test
    fun brzycki_handles_zero_and_high_rep_guard() {
        assertEquals(0.0, StrengthFormulas.brzycki1RM(100.0, 0), 0.0001)
        assertEquals(100.0, StrengthFormulas.brzycki1RM(100.0, 37), 0.0001)
        assertTrue(StrengthFormulas.brzycki1RM(100.0, 10) > 100.0)
    }

    @Test
    fun estimated_1rm_is_average_of_epley_and_brzycki() {
        val epley = StrengthFormulas.epley1RM(100.0, 5)
        val brzycki = StrengthFormulas.brzycki1RM(100.0, 5)
        assertEquals(
            (epley + brzycki) / 2.0,
            StrengthFormulas.estimated1RM(100.0, 5),
            0.0001
        )
    }

    @Test
    fun total_volume_excludes_warmups_by_default() {
        val sets = listOf(
            SetEntry(workoutSessionId = 1, exerciseId = 1, setIndex = 1, weight = 20.0, reps = 10, isWarmup = true),
            SetEntry(workoutSessionId = 1, exerciseId = 1, setIndex = 2, weight = 50.0, reps = 8, isWarmup = false)
        )

        assertEquals(400.0, StrengthFormulas.totalVolume(sets), 0.0001)
        assertEquals(600.0, StrengthFormulas.totalVolume(sets, includeWarmups = true), 0.0001)
    }

    @Test
    fun best_estimated_1rm_ignores_warmups() {
        val sets = listOf(
            SetEntry(workoutSessionId = 1, exerciseId = 1, setIndex = 1, weight = 150.0, reps = 1, isWarmup = true),
            SetEntry(workoutSessionId = 1, exerciseId = 1, setIndex = 2, weight = 100.0, reps = 5, isWarmup = false),
            SetEntry(workoutSessionId = 1, exerciseId = 1, setIndex = 3, weight = 105.0, reps = 5, isWarmup = false)
        )

        assertEquals(
            StrengthFormulas.estimated1RM(105.0, 5),
            StrengthFormulas.bestEstimated1RM(sets),
            0.0001
        )
    }

    @Test
    fun percent_change_handles_zero_baseline() {
        assertEquals(0.0, StrengthFormulas.percentChange(0.0, 100.0), 0.0001)
        assertEquals(25.0, StrengthFormulas.percentChange(80.0, 100.0), 0.0001)
    }
}

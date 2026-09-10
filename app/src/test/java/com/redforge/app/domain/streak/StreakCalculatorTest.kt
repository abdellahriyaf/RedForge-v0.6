package com.redforge.app.domain.streak

import com.redforge.app.data.local.entities.WorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class StreakCalculatorTest {
    private val zone = TimeZone.getTimeZone("UTC")

    @Test
    fun `empty history has no streak`() {
        val result = StreakCalculator.compute(emptyList(), nowMillis = millis(2026, Calendar.SEPTEMBER, 10, 18), timeZone = zone)
        assertEquals(0, result.current)
        assertEquals(0, result.longest)
        assertEquals(0L, result.lastCompletedDayMillis)
    }

    @Test
    fun `multiple sessions on one day count once`() {
        val mondayMorning = millis(2026, Calendar.SEPTEMBER, 7, 9)
        val mondayEvening = millis(2026, Calendar.SEPTEMBER, 7, 18)
        val tuesday = millis(2026, Calendar.SEPTEMBER, 8, 18)

        val sessions = listOf(
            completed(mondayMorning),
            completed(mondayEvening),
            completed(tuesday)
        )

        val result = StreakCalculator.compute(sessions, nowMillis = tuesday, timeZone = zone)
        assertEquals(2, result.current)
        assertEquals(2, result.longest)
    }

    @Test
    fun `gap larger than maximum breaks current streak`() {
        val monday = millis(2026, Calendar.SEPTEMBER, 7, 18)
        val thursday = millis(2026, Calendar.SEPTEMBER, 10, 18)

        val result = StreakCalculator.compute(
            listOf(completed(monday), completed(thursday)),
            maxGapDays = 2,
            nowMillis = thursday,
            timeZone = zone
        )

        assertEquals(1, result.current)
        assertEquals(1, result.longest)
    }

    @Test
    fun `timezone date boundaries are evaluated in the requested zone`() {
        val losAngeles = TimeZone.getTimeZone("America/Los_Angeles")
        val first = millis(losAngeles, 2026, Calendar.SEPTEMBER, 8, 6, 55)
        val second = millis(losAngeles, 2026, Calendar.SEPTEMBER, 8, 23, 55)
        val next = millis(losAngeles, 2026, Calendar.SEPTEMBER, 9, 0, 5)

        val result = StreakCalculator.compute(
            listOf(completed(first), completed(second), completed(next)),
            nowMillis = next,
            timeZone = losAngeles
        )

        assertEquals(2, result.current)
        assertEquals(2, result.longest)
    }

    @Test
    fun `maximum allowed gap remains connected`() {
        val monday = millis(2026, Calendar.SEPTEMBER, 7, 18)
        val wednesday = millis(2026, Calendar.SEPTEMBER, 9, 18)

        val result = StreakCalculator.compute(
            listOf(completed(monday), completed(wednesday)),
            maxGapDays = 2,
            nowMillis = wednesday,
            timeZone = zone
        )

        assertEquals(2, result.current)
        assertEquals(2, result.longest)
    }

    private fun completed(startedAt: Long) = WorkoutSession(
        splitDayId = 1L,
        splitDayNameSnapshot = "Training",
        startedAt = startedAt,
        completed = true
    )

    private fun millis(year: Int, month: Int, day: Int, hour: Int): Long =
        millis(zone, year, month, day, hour, 0)

    private fun millis(timeZone: TimeZone, year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance(timeZone).apply {
            clear()
            set(year, month, day, hour, minute, 0)
        }.timeInMillis
}

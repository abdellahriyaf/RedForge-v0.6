package com.redforge.app.domain.streak

import com.redforge.app.data.local.entities.WorkoutSession
import java.util.Calendar
import java.util.TimeZone

data class StreakResult(
    val current: Int,
    val longest: Int,
    val lastCompletedDayMillis: Long
)

/**
 * Computes a day-based training streak from completed sessions.
 * Multiple sessions on one calendar day count only once.
 */
object StreakCalculator {

    fun compute(
        sessions: List<WorkoutSession>,
        maxGapDays: Int = 2,
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): StreakResult {
        if (sessions.isEmpty()) return StreakResult(0, 0, 0L)

        val uniqueDays = HashSet<Long>()
        var lastCompletedAt = 0L
        for (session in sessions) {
            if (!session.completed) continue
            uniqueDays += civilDay(session.startedAt, timeZone)
            if (session.startedAt > lastCompletedAt) lastCompletedAt = session.startedAt
        }
        if (uniqueDays.isEmpty()) return StreakResult(0, 0, 0L)

        val days = uniqueDays.toLongArray().also { it.sort() }
        var run = 1
        var longest = 1
        for (index in 1 until days.size) {
            val gap = days[index] - days[index - 1]
            run = if (gap <= maxGapDays) run + 1 else 1
            if (run > longest) longest = run
        }

        val today = civilDay(nowMillis, timeZone)
        val daysSinceLast = today - days.last()
        val liveCurrent = if (daysSinceLast > maxGapDays) 0 else run

        return StreakResult(
            current = liveCurrent,
            longest = longest,
            lastCompletedDayMillis = lastCompletedAt
        )
    }

    private fun civilDay(millis: Long, timeZone: TimeZone): Long {
        val calendar = Calendar.getInstance(timeZone).apply { timeInMillis = millis }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return gregorianEpochDay(year, month, day)
    }

    /** Constant-time proleptic Gregorian date -> epoch-day conversion. */
    private fun gregorianEpochDay(year: Int, month: Int, day: Int): Long {
        var y = year.toLong()
        val m = month.toLong()
        y -= if (m <= 2) 1 else 0
        val era = Math.floorDiv(y, 400L)
        val yoe = y - era * 400L
        val mp = m + if (m > 2) -3 else 9
        val doy = (153L * mp + 2L) / 5L + day - 1L
        val doe = yoe * 365L + yoe / 4L - yoe / 100L + doy
        return era * 146097L + doe - 719468L
    }
}

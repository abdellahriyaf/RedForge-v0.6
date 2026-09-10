package com.redforge.app.domain.streak

import com.redforge.app.data.local.entities.WorkoutSession
import java.time.Instant
import java.time.ZoneId

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
        timeZone: java.util.TimeZone = java.util.TimeZone.getDefault()
    ): StreakResult {
        if (sessions.isEmpty()) return StreakResult(0, 0, 0L)

        val zone = timeZone.toZoneId()
        val uniqueDays = HashSet<Long>()
        var lastCompletedAt = 0L
        for (session in sessions) {
            if (!session.completed) continue
            uniqueDays += Instant.ofEpochMilli(session.startedAt).atZone(zone).toLocalDate().toEpochDay()
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

        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate().toEpochDay()
        val daysSinceLast = today - days.last()
        val liveCurrent = if (daysSinceLast > maxGapDays) 0 else run

        return StreakResult(
            current = liveCurrent,
            longest = longest,
            lastCompletedDayMillis = lastCompletedAt
        )
    }
}

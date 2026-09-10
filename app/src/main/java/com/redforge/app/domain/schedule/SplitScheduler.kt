package com.redforge.app.domain.schedule

import com.redforge.app.data.local.entities.SplitDay
import com.redforge.app.data.local.entities.WorkoutSession
import java.util.Calendar
import java.util.TimeZone

/**
 * Pure schedule logic for the active split.
 *
 * The current RedForge scheduler treats a split as a planned calendar cycle. Rest days remain
 * in the cycle, so a three-day cycle of Push / Pull / Rest really does place
 * Rest on the third calendar day. Missed days advance the calendar too; the
 * plan belongs to the calendar rather than getting stuck waiting for a rest
 * day to be "completed" as a workout.
 */
object SplitScheduler {

    fun nextDay(
        days: List<SplitDay>,
        recentSessions: List<WorkoutSession>,
        targetTimeMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault(),
        scheduleAnchorStartMillis: Long? = null
    ): SplitDay? = plannedDayForDate(days, recentSessions, targetTimeMillis, timeZone, scheduleAnchorStartMillis)

    fun plannedDayForDate(
        days: List<SplitDay>,
        recentSessions: List<WorkoutSession>,
        targetTimeMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault(),
        scheduleAnchorStartMillis: Long? = null
    ): SplitDay? {
        if (days.isEmpty()) return null
        val ordered = days.sortedBy { it.dayOrder }
        val dayIdToIndex = ordered.mapIndexed { index, day -> day.id to index }.toMap()
        val targetDay = civilDay(targetTimeMillis, timeZone)
        val anchorDay = scheduleAnchorStartMillis?.let { civilDay(it, timeZone) }

        if (anchorDay != null && targetDay < anchorDay) return null

        val lastCompletedForThisSplit = recentSessions.asSequence()
            .filter { it.completed && it.splitDayId != null && dayIdToIndex.containsKey(it.splitDayId) }
            .filter { session -> anchorDay == null || civilDay(session.startedAt, timeZone) >= anchorDay }
            .maxByOrNull { it.startedAt }

        if (lastCompletedForThisSplit == null) {
            if (anchorDay == null) return ordered.first()
            val elapsed = targetDay - anchorDay
            return ordered[Math.floorMod(elapsed.toInt(), ordered.size)]
        }

        val lastIndex = dayIdToIndex[lastCompletedForThisSplit.splitDayId] ?: return ordered.first()
        val elapsedCalendarDays = targetDay - civilDay(lastCompletedForThisSplit.startedAt, timeZone)
        if (elapsedCalendarDays < 0) return ordered.first()
        return ordered[(lastIndex + elapsedCalendarDays.toInt()) % ordered.size]
    }

    fun isBeforeAnchor(
        anchorStartMillis: Long,
        targetTimeMillis: Long,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Boolean = civilDay(targetTimeMillis, timeZone) < civilDay(anchorStartMillis, timeZone)

    private fun civilDay(millis: Long, timeZone: TimeZone): Long {
        val calendar = Calendar.getInstance(timeZone).apply { timeInMillis = millis }
        return gregorianEpochDay(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
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

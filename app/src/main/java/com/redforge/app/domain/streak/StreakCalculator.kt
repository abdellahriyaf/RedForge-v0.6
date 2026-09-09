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
        val completed = sessions.filter { it.completed }.sortedBy { it.startedAt }
        if (completed.isEmpty()) return StreakResult(0, 0, 0L)

        val uniqueDays = completed
            .map { CalendarDay.from(it.startedAt, timeZone) }
            .distinct()
            .sorted()

        var current = 1
        var longest = 1
        for (index in 1 until uniqueDays.size) {
            val gap = uniqueDays[index].differenceFrom(uniqueDays[index - 1], timeZone)
            current = if (gap <= maxGapDays) current + 1 else 1
            longest = maxOf(longest, current)
        }

        val today = CalendarDay.from(nowMillis, timeZone)
        val daysSinceLast = today.differenceFrom(uniqueDays.last(), timeZone)
        val liveCurrent = if (daysSinceLast > maxGapDays) 0 else current

        val lastDayMillis = completed.last().startedAt
        return StreakResult(
            current = liveCurrent,
            longest = longest,
            lastCompletedDayMillis = lastDayMillis
        )
    }

    private data class CalendarDay(val era: Int, val year: Int, val dayOfYear: Int) : Comparable<CalendarDay> {
        companion object {
            fun from(millis: Long, timeZone: TimeZone): CalendarDay {
                val calendar = Calendar.getInstance(timeZone).apply { timeInMillis = millis }
                return CalendarDay(
                    era = calendar.get(Calendar.ERA),
                    year = calendar.get(Calendar.YEAR),
                    dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                )
            }
        }

        override fun compareTo(other: CalendarDay): Int {
            compareValuesBy(this, other, { it.era }, { it.year }, { it.dayOfYear })
                .let { return it }
        }

        fun differenceFrom(other: CalendarDay, timeZone: TimeZone): Int {
            val start = toCalendar(timeZone)
            start.set(Calendar.ERA, era)
            start.set(Calendar.YEAR, year)
            start.set(Calendar.DAY_OF_YEAR, dayOfYear)
            val end = toCalendar(timeZone)
            end.set(Calendar.ERA, other.era)
            end.set(Calendar.YEAR, other.year)
            end.set(Calendar.DAY_OF_YEAR, other.dayOfYear)
            var cursor = end.clone() as Calendar
            var difference = 0
            if (!sameCalendarDate(cursor, start)) {
                val forward = cursor.before(start)
                while (!sameCalendarDate(cursor, start)) {
                    cursor.add(Calendar.DAY_OF_YEAR, if (forward) 1 else -1)
                    difference += if (forward) 1 else -1
                    if (kotlin.math.abs(difference) > 100_000) break
                }
            }
            return difference
        }
        private fun toCalendar(timeZone: TimeZone): Calendar =
            Calendar.getInstance(timeZone).apply {
                clear()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
    }

    private fun sameCalendarDate(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.ERA) == b.get(Calendar.ERA) &&
            a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}

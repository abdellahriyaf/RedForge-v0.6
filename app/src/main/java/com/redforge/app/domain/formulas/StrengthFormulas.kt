package com.redforge.app.domain.formulas

import com.redforge.app.data.local.entities.SetEntry
import kotlin.math.roundToInt

object StrengthFormulas {

    fun epley1RM(weight: Double, reps: Int): Double {
        if (reps <= 0) return 0.0
        if (reps == 1) return weight
        return weight * (1 + reps / 30.0)
    }

    fun brzycki1RM(weight: Double, reps: Int): Double {
        if (reps <= 0) return 0.0
        if (reps == 1) return weight
        if (reps >= 37) return weight
        return weight * 36.0 / (37.0 - reps)
    }

    fun estimated1RM(weight: Double, reps: Int): Double {
        if (reps <= 1) return weight
        return (epley1RM(weight, reps) + brzycki1RM(weight, reps)) / 2.0
    }

    /** Allocation-free hot path for workout/history calculations. */
    fun totalVolume(sets: List<SetEntry>, includeWarmups: Boolean = false): Double {
        var total = 0.0
        for (set in sets) {
            if (includeWarmups || !set.isWarmup) total += set.weight * set.reps
        }
        return total
    }

    /** Allocation-free best-estimate scan. */
    fun bestEstimated1RM(sets: List<SetEntry>): Double {
        var best = 0.0
        for (set in sets) {
            if (set.isWarmup) continue
            val estimate = estimated1RM(set.weight, set.reps)
            if (estimate > best) best = estimate
        }
        return best
    }

    fun percentChange(previous: Double, current: Double): Double {
        if (previous <= 0.0) return 0.0
        return ((current - previous) / previous) * 100.0
    }

    fun displayRounded(value: Double): Int = value.roundToInt()
}

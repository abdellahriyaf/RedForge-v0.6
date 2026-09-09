package com.redforge.app.domain.formulas

import kotlin.math.roundToInt

data class WarmupSet(val percentOfWorking: Int, val weight: Double, val reps: Int)

/**
 * Warm-up ramp and plate-loading helpers — the "scientific tracking" the
 * user asked for extends to session prep, not just post-hoc analysis.
 */
object TrainingCalculators {

    /**
     * A standard 3-step ramp (40/60/80% of the working weight) with
     * descending reps, rounded to the nearest sensible increment so the
     * numbers are actually loadable on a bar.
     */
    fun warmupRamp(workingWeight: Double, roundingIncrement: Double = 2.5): List<WarmupSet> {
        if (workingWeight <= 0.0) return emptyList()
        val steps = listOf(40 to 10, 60 to 8, 80 to 5)
        return steps.map { (percent, reps) ->
            val raw = workingWeight * percent / 100.0
            val rounded = (raw / roundingIncrement).roundToInt() * roundingIncrement
            WarmupSet(percentOfWorking = percent, weight = rounded, reps = reps)
        }
    }

    private val KG_PLATES = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
    private val LB_PLATES = listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5, 1.25)

    /**
     * Greedy plate-loading calculation for one side of the bar. Returns an
     * empty list if the target is at or below the bar weight, and may
     * under-fill by a small residual if the target isn't reachable exactly
     * with the given plate set (residual is reported so the UI can show it).
     */
    fun platesPerSide(targetWeight: Double, barWeight: Double, useKg: Boolean): Pair<List<Double>, Double> {
        var remaining = (targetWeight - barWeight) / 2.0
        if (remaining <= 0.0) return emptyList<Double>() to 0.0

        val plateSet = if (useKg) KG_PLATES else LB_PLATES
        val result = mutableListOf<Double>()
        for (plate in plateSet) {
            while (remaining + 1e-6 >= plate) {
                result.add(plate)
                remaining -= plate
            }
        }
        return result to remaining
    }
}

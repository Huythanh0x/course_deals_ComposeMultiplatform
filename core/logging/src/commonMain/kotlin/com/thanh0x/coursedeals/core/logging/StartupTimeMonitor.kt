package com.thanh0x.coursedeals.core.logging

import kotlin.time.TimeSource

object StartupTimeMonitor {

    enum class StepName(val key: String) {
        Logging("logging"),
        KoinModules("koin_modules"),
        AuthSession("auth_session"),
        FirstFrame("first_frame"),
    }

    private val lock = Any()
    private var appStartMark: TimeSource.Monotonic.ValueTimeMark? = null
    private val stepStartMarks = mutableMapOf<StepName, TimeSource.Monotonic.ValueTimeMark>()
    private val stepDurationsMs = linkedMapOf<StepName, Long>()
    private var firstFrameReported: Boolean = false
    private var lastReport: StartupMetricsReport? = null

    fun onAppStart() {
        syncLock(lock) {
            if (appStartMark == null) {
                appStartMark = TimeSource.Monotonic.markNow()
            }
        }
    }

    fun startStep(step: StepName) {
        syncLock(lock) {
            if (appStartMark == null) {
                appStartMark = TimeSource.Monotonic.markNow()
            }
            stepStartMarks[step] = TimeSource.Monotonic.markNow()
        }
    }

    fun endStep(step: StepName): Long {
        return syncLock(lock) {
            val startMark = stepStartMarks.remove(step)
            val durationMs = startMark?.elapsedNow()?.inWholeMilliseconds?.coerceAtLeast(0L)
                ?: stepDurationsMs[step]
                ?: 0L
            stepDurationsMs[step] = durationMs
            durationMs
        }
    }

    inline fun <T> trackStep(step: StepName, block: () -> T): T {
        startStep(step)
        return try {
            block()
        } finally {
            endStep(step)
        }
    }

    fun onFirstFrameRendered(): StartupMetricsReport {
        val existingReport = syncLock(lock) {
            if (firstFrameReported) lastReport else null
        }
        if (existingReport != null) {
            return existingReport
        }

        val (stepsMap, totalMs) = syncLock(lock) {
            val nowMark = TimeSource.Monotonic.markNow()
            val startMark = appStartMark ?: nowMark.also { appStartMark = it }

            val firstFrameStart = stepStartMarks.remove(StepName.FirstFrame)
            if (firstFrameStart != null) {
                stepDurationsMs[StepName.FirstFrame] =
                    firstFrameStart.elapsedNow().inWholeMilliseconds.coerceAtLeast(0L)
            } else if (!stepDurationsMs.containsKey(StepName.FirstFrame)) {
                val sumPrevious = stepDurationsMs.values.sum()
                val elapsedTotal = startMark.elapsedNow().inWholeMilliseconds.coerceAtLeast(0L)
                stepDurationsMs[StepName.FirstFrame] = (elapsedTotal - sumPrevious).coerceAtLeast(0L)
            }

            val orderedSteps = linkedMapOf<String, Long>()
            for (step in StepName.entries) {
                orderedSteps[step.key] = stepDurationsMs[step] ?: 0L
            }

            val measuredTotal = startMark.elapsedNow().inWholeMilliseconds.coerceAtLeast(0L)
            val stepsSum = orderedSteps.values.sum()
            val resolvedTotal = maxOf(measuredTotal, stepsSum)

            firstFrameReported = true
            orderedSteps to resolvedTotal
        }

        val report = TracingReporter.recordStartupMetrics(
            stepsMs = stepsMap,
            totalMs = totalMs,
        )
        syncLock(lock) {
            lastReport = report
        }

        val summary = StepName.entries.joinToString(", ") { step ->
            "${step.key}=${stepsMap[step.key] ?: 0L}ms"
        }
        AppLogger.tag("StartupTimeMonitor").info {
            "ColdStart complete in ${totalMs}ms [$summary]"
        }

        return report
    }

    fun getRecordedStepsMs(): Map<String, Long> = syncLock(lock) {
        StepName.entries.associate { step ->
            step.key to (stepDurationsMs[step] ?: 0L)
        }
    }

    fun isFirstFrameReported(): Boolean = syncLock(lock) { firstFrameReported }

    fun resetForTesting() {
        syncLock(lock) {
            appStartMark = null
            stepStartMarks.clear()
            stepDurationsMs.clear()
            firstFrameReported = false
            lastReport = null
        }
    }
}

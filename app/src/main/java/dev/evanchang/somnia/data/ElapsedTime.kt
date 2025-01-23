package dev.evanchang.somnia.data

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

interface ElapsedTime {
    fun elapsedTime(): Duration
    fun elapsedTimeString(): String {
        val elapsedTime = elapsedTime()
        return if (elapsedTime < (1).toDuration(DurationUnit.MINUTES)) {
            "${elapsedTime.inWholeSeconds}s"
        } else if (elapsedTime < (1).toDuration(DurationUnit.HOURS)) {
            "${elapsedTime.inWholeMinutes}m"
        } else if (elapsedTime < (1).toDuration(DurationUnit.DAYS)) {
            "${elapsedTime.inWholeHours}h"
        } else if (elapsedTime < (365).toDuration(DurationUnit.DAYS)) {
            "${elapsedTime.inWholeDays}d"
        } else {
            "${elapsedTime.inWholeDays / 365}y"
        }
    }
}
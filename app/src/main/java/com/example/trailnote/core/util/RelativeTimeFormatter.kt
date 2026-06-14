package com.example.trailnote.core.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object RelativeTimeFormatter {
    fun format(timeMillis: Long): String {
        val elapsed = Duration.between(Instant.ofEpochMilli(timeMillis), Instant.now()).coerceAtLeast(Duration.ZERO)
        val minutes = elapsed.toMinutes()
        if (minutes < 1) return "\uBC29\uAE08 \uC804"
        if (minutes < 60) return "${minutes}\uBD84 \uC804"
        val hours = elapsed.toHours()
        if (hours < 24) return "${hours}\uC2DC\uAC04 \uC804"
        val days = elapsed.toDays()
        if (days < 7) return "${days}\uC77C \uC804"
        if (days < 30) return "${days / 7}\uC8FC \uC804"
        if (days < 365) return "${days / 30}\uAC1C\uC6D4 \uC804"
        return "${days / 365}\uB144 \uC804"
    }

    fun latestEpochMillis(values: Iterable<String?>): Long? {
        return values
            .mapNotNull { value -> value?.takeIf(String::isNotBlank)?.toEpochMillis() }
            .maxOrNull()
    }

    private fun String.toEpochMillis(): Long? {
        return runCatching {
            LocalDateTime.parse(this).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.getOrElse {
            runCatching {
                LocalDate.parse(this).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }.getOrNull()
        }
    }
}

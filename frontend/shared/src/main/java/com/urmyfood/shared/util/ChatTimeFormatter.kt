package com.urmyfood.shared.util

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ChatTimeFormatter {
    private val appZone: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun formatTime(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return try {
            if (value.contains("+") || value.endsWith("Z")) {
                OffsetDateTime.parse(value)
                    .atZoneSameInstant(appZone)
                    .format(timeFormatter)
            } else {
                rawTime(value)
            }
        } catch (e: Exception) {
            rawTime(value)
        }
    }

    private fun rawTime(value: String): String {
        val tIndex = value.indexOf("T")
        return if (tIndex >= 0 && value.length >= tIndex + 6) {
            value.substring(tIndex + 1, tIndex + 6)
        } else if (value.length >= 5) {
            value.take(5)
        } else {
            value
        }
    }
}

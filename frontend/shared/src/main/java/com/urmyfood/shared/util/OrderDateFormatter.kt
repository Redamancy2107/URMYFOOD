package com.urmyfood.shared.util

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object OrderDateFormatter {
    private const val UNKNOWN_TIME = "Không rõ thời gian"
    private val appZone: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    private val displayFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))

    fun format(value: String?): String {
        if (value.isNullOrBlank()) return UNKNOWN_TIME
        return try {
            if (value.contains("+") || value.endsWith("Z")) {
                OffsetDateTime.parse(value)
                    .atZoneSameInstant(appZone)
                    .format(displayFormatter)
            } else {
                // E.g., "2025-06-20T14:30:00"
                java.time.LocalDateTime.parse(value)
                    .format(displayFormatter)
            }
        } catch (e: Exception) {
            runCatching {
                if (value.length >= 16 && value.contains("T")) {
                    val datePart = value.substring(0, 10) // yyyy-MM-dd
                    val timePart = value.substring(11, 16) // HH:mm
                    val dateParts = datePart.split("-")
                    if (dateParts.size == 3) {
                        "$timePart ${dateParts[2]}/${dateParts[1]}/${dateParts[0]}"
                    } else {
                        "$timePart $datePart"
                    }
                } else {
                    value
                }
            }.getOrDefault(UNKNOWN_TIME)
        }
    }
}

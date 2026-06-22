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
        return runCatching {
            OffsetDateTime.parse(value)
                .atZoneSameInstant(appZone)
                .format(displayFormatter)
        }.getOrDefault(UNKNOWN_TIME)
    }
}

package com.urmyfood.shared.util

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object TimeUtils {

    fun isWithinOpeningHours(openingHours: String?): Boolean {
        if (openingHours.isNullOrBlank()) {
            return true
        }
        try {
            val parts = openingHours.split("-")
            if (parts.size != 2) {
                return true
            }
            val startStr = parts[0].trim()
            val endStr = parts[1].trim()

            val nowVietnam = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
            val nowTime = nowVietnam.toLocalTime()

            val startTime = LocalTime.parse(startStr)
            val endTime = LocalTime.parse(endStr)

            return if (startTime.isBefore(endTime)) {
                !nowTime.isBefore(startTime) && !nowTime.isAfter(endTime)
            } else {
                !nowTime.isBefore(startTime) || !nowTime.isAfter(endTime)
            }
        } catch (e: Exception) {
            return true
        }
    }

    fun isShopCurrentlyOpen(isOpen: Boolean?, openingHours: String?): Boolean {
        if (isOpen != true) return false
        return isWithinOpeningHours(openingHours)
    }
    
}

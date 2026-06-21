package com.urmyfood.shared.util

import org.junit.Assert.assertEquals
import org.junit.Test

class OrderDateFormatterTest {

    @Test
    fun `format converts offset date time to Vietnam display time`() {
        val formatted = OrderDateFormatter.format("2026-06-22T07:05:00Z")

        assertEquals("14:05 22/06/2026", formatted)
    }

    @Test
    fun `format returns fallback for blank or invalid values`() {
        assertEquals("Không rõ thời gian", OrderDateFormatter.format(null))
        assertEquals("Không rõ thời gian", OrderDateFormatter.format(""))
        assertEquals("Không rõ thời gian", OrderDateFormatter.format("not-a-date"))
    }
}

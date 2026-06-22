package com.urmyfood.user.presentation.main.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class OrderHistoryStatusMapperTest {

    @Test
    fun `pending maps to pending tab instead of processing`() {
        assertEquals(
            OrderHistoryStatusMapper.TAB_PENDING,
            OrderHistoryStatusMapper.tabFor("PENDING")
        )
    }

    @Test
    fun `picking up stays in processing tab instead of delivering`() {
        assertEquals(
            OrderHistoryStatusMapper.TAB_PROCESSING,
            OrderHistoryStatusMapper.tabFor("PICKING_UP")
        )
    }

    @Test
    fun `delivering maps to delivering tab`() {
        assertEquals(
            OrderHistoryStatusMapper.TAB_DELIVERING,
            OrderHistoryStatusMapper.tabFor("DELIVERING")
        )
    }
}

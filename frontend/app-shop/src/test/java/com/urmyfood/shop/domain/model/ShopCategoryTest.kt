package com.urmyfood.shop.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ShopCategoryTest {

    @Test
    fun `entries count is 8`() {
        assertEquals(8, ShopCategory.entries.size)
    }

    @Test
    fun `all display names are correct`() {
        assertEquals("Cơm", ShopCategory.COM.displayName)
        assertEquals("Phở", ShopCategory.PHO.displayName)
        assertEquals("Bún", ShopCategory.BUN.displayName)
        assertEquals("Bánh mì", ShopCategory.BANH_MI.displayName)
        assertEquals("Đồ uống", ShopCategory.DO_UONG.displayName)
        assertEquals("Nướng", ShopCategory.NUONG.displayName)
        assertEquals("Đồ chay", ShopCategory.CHAY.displayName)
        assertEquals("Khác", ShopCategory.KHAC.displayName)
    }
}

package com.example.sairo14.core.map

import org.junit.Assert.assertEquals
import org.junit.Test

class SairoMapViewportPaddingPolicyTest {

    @Test
    fun `상단과 하단 여백 합계가 지도 높이를 넘지 않도록 제한한다`() {
        val padding = SairoMapViewportPaddingPx(top = 157, bottom = 697)

        val result = padding.clampToViewport(
            viewportWidth = 360,
            viewportHeight = 766,
        )

        assertEquals(157, result.top)
        assertEquals(609, result.bottom)
    }

    @Test
    fun `측정 전 지도에는 0 여백을 전달한다`() {
        val result = SairoMapViewportPaddingPx(
            left = 10,
            top = 20,
            right = 30,
            bottom = 40,
        ).clampToViewport(
            viewportWidth = 0,
            viewportHeight = 0,
        )

        assertEquals(SairoMapViewportPaddingPx(), result)
    }
}

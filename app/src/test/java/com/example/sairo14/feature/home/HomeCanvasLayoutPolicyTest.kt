package com.example.sairo14.feature.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeCanvasLayoutPolicyTest {

    @Test
    fun `카드가 없으면 배치와 이동 한계가 모두 비어 있다`() {
        val layout = calculate(cardCount = 0)

        assertEquals(emptyList<HomeResolvedSavedTripPlacement>(), layout.cardPlacements)
        assertPanBoundsEquals(HomeCanvasPanBounds(), layout.panBounds)
    }

    @Test
    fun `카드 한 장은 실제로 화면 밖에 배치된 왼쪽과 위쪽만 이동할 수 있다`() {
        val layout = calculate(cardCount = 1)

        assertEquals(1, layout.cardPlacements.size)
        assertPanBoundsEquals(
            HomeCanvasPanBounds(
                minX = 0f,
                maxX = 75f,
                minY = 0f,
                maxY = 139f,
            ),
            layout.panBounds,
        )
    }

    @Test
    fun `카드 네 장은 기존 네 모서리 배치에 맞춰 네 방향 이동 한계를 계산한다`() {
        val layout = calculate(cardCount = 4)

        assertEquals(4, layout.cardPlacements.size)
        assertPanBoundsEquals(
            HomeCanvasPanBounds(
                minX = -159f,
                maxX = 75f,
                minY = -156f,
                maxY = 139f,
            ),
            layout.panBounds,
        )
    }

    @Test
    fun `다섯 번째 왼쪽 바깥 카드는 필요한 가로 이동 범위만 확장한다`() {
        val fourCards = calculate(cardCount = 4)
        val fiveCards = calculate(cardCount = 5)

        assertEquals(5, fiveCards.cardPlacements.size)
        assertEquals(fourCards.panBounds.minX, fiveCards.panBounds.minX)
        assertEquals(126f, fiveCards.panBounds.maxX, FloatTolerance)
        assertEquals(fourCards.panBounds.minY, fiveCards.panBounds.minY)
        assertEquals(fourCards.panBounds.maxY, fiveCards.panBounds.maxY)
    }

    @Test
    fun `여덟 장은 네 방향 바깥 카드를 모두 탐색할 수 있는 한계를 계산한다`() {
        val layout = calculate(cardCount = 8)

        assertEquals(8, layout.cardPlacements.size)
        assertPanBoundsEquals(
            HomeCanvasPanBounds(
                minX = -159f,
                maxX = 126f,
                minY = -211f,
                maxY = 271f,
            ),
            layout.panBounds,
        )
    }

    @Test
    fun `시각 여유는 카드 그림자와 회전을 포함해 이동 한계를 확장한다`() {
        val layout = calculate(cardCount = 1, visualOverflow = 12f)

        assertPanBoundsEquals(
            HomeCanvasPanBounds(
                minX = 0f,
                maxX = 87f,
                minY = 0f,
                maxY = 151f,
            ),
            layout.panBounds,
        )
    }

    private fun calculate(
        cardCount: Int,
        visualOverflow: Float = 0f,
    ): HomeCanvasLayout = HomeCanvasLayoutPolicy.calculate(
        canvasSize = CanvasSize,
        visibleViewport = VisibleViewport,
        cardSize = CardSize,
        placements = HomeSavedTripPlacements.create(CardSize),
        cardCount = cardCount,
        visualOverflow = visualOverflow,
    )

    private fun assertPanBoundsEquals(
        expected: HomeCanvasPanBounds,
        actual: HomeCanvasPanBounds,
    ) {
        assertEquals(expected.minX, actual.minX, FloatTolerance)
        assertEquals(expected.maxX, actual.maxX, FloatTolerance)
        assertEquals(expected.minY, actual.minY, FloatTolerance)
        assertEquals(expected.maxY, actual.maxY, FloatTolerance)
    }

    private companion object {
        const val FloatTolerance = 0.001f
        val CanvasSize = HomeCanvasSize(width = 360f, height = 800f)
        val VisibleViewport = HomeCanvasRect(
            left = 0f,
            top = 100f,
            right = 360f,
            bottom = 760f,
        )
        val CardSize = HomeCanvasSize(width = 150f, height = 195f)
    }
}

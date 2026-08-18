package com.example.sairo14.feature.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun `여덟 장을 초과해도 제공된 최대 여덟 개 슬롯만 반환한다`() {
        val eightCards = calculate(cardCount = 8)
        val moreThanEightCards = calculate(cardCount = 9)

        assertEquals(8, moreThanEightCards.cardPlacements.size)
        assertEquals(eightCards.cardPlacements, moreThanEightCards.cardPlacements)
        assertPanBoundsEquals(eightCards.panBounds, moreThanEightCards.panBounds)
    }

    @Test
    fun `작은 화면에서도 1개 4개 8개 모든 카드에 정책 이동 범위로 접근할 수 있다`() {
        listOf(
            SmallScreenCase(
                cardCount = 1,
                panBounds = HomeCanvasPanBounds(maxX = 75f, maxY = 111f),
            ),
            SmallScreenCase(
                cardCount = 4,
                panBounds = HomeCanvasPanBounds(
                    minX = -159f,
                    maxX = 75f,
                    minY = -116f,
                    maxY = 111f,
                ),
            ),
            SmallScreenCase(
                cardCount = 8,
                panBounds = HomeCanvasPanBounds(
                    minX = -159f,
                    maxX = 126f,
                    minY = -171f,
                    maxY = 243f,
                ),
            ),
        ).forEach { expected ->
            val layout = calculate(
                cardCount = expected.cardCount,
                canvasSize = SmallCanvasSize,
                visibleViewport = SmallVisibleViewport,
            )

            assertEquals(expected.cardCount, layout.cardPlacements.size)
            assertPanBoundsEquals(expected.panBounds, layout.panBounds)
            layout.cardPlacements.forEach { placement ->
                assertCanReachVisibleViewport(placement.cardBounds, layout.panBounds, SmallVisibleViewport)
            }
        }
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
        canvasSize: HomeCanvasSize = CanvasSize,
        visibleViewport: HomeCanvasRect = VisibleViewport,
    ): HomeCanvasLayout = HomeCanvasLayoutPolicy.calculate(
        canvasSize = canvasSize,
        visibleViewport = visibleViewport,
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

    private fun assertCanReachVisibleViewport(
        cardBounds: HomeCanvasRect,
        panBounds: HomeCanvasPanBounds,
        visibleViewport: HomeCanvasRect,
    ) {
        val offsetX = when {
            cardBounds.right < visibleViewport.left -> panBounds.maxX
            cardBounds.left > visibleViewport.right -> panBounds.minX
            else -> 0f
        }
        val offsetY = when {
            cardBounds.bottom < visibleViewport.top -> panBounds.maxY
            cardBounds.top > visibleViewport.bottom -> panBounds.minY
            else -> 0f
        }

        assertTrue(cardBounds.right + offsetX >= visibleViewport.left)
        assertTrue(cardBounds.left + offsetX <= visibleViewport.right)
        assertTrue(cardBounds.bottom + offsetY >= visibleViewport.top)
        assertTrue(cardBounds.top + offsetY <= visibleViewport.bottom)
    }

    private data class SmallScreenCase(
        val cardCount: Int,
        val panBounds: HomeCanvasPanBounds,
    )

    private companion object {
        const val FloatTolerance = 0.001f
        val CanvasSize = HomeCanvasSize(width = 360f, height = 800f)
        val VisibleViewport = HomeCanvasRect(
            left = 0f,
            top = 100f,
            right = 360f,
            bottom = 760f,
        )
        val SmallCanvasSize = HomeCanvasSize(width = 320f, height = 568f)
        val SmallVisibleViewport = HomeCanvasRect(
            left = 0f,
            top = 72f,
            right = 320f,
            bottom = 568f,
        )
        val CardSize = HomeCanvasSize(width = 150f, height = 195f)
    }
}

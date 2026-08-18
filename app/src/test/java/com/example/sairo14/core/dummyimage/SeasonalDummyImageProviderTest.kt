package com.example.sairo14.core.dummyimage

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonalDummyImageProviderTest {
    @Test
    fun `월 경계에 맞춰 계절을 판정한다`() {
        assertEquals(DummyImageSeason.Winter, seasonOf(2))
        assertEquals(DummyImageSeason.Spring, seasonOf(3))
        assertEquals(DummyImageSeason.Summer, seasonOf(6))
        assertEquals(DummyImageSeason.Autumn, seasonOf(9))
        assertEquals(DummyImageSeason.Winter, seasonOf(12))
    }

    @Test
    fun `온보딩과 홈에는 중복되지 않는 여덟 장을 배정한다`() {
        val result = SeasonalDummyImageSelector.select(
            season = DummyImageSeason.Summer,
            random = Random(20260818),
        )

        val onboardingImages = result.onboardingPairs.flatMap { pair ->
            listOf(pair.backImageRes, pair.frontImageRes)
        }
        val allImages = onboardingImages + listOf(
            result.homePair.backImageRes,
            result.homePair.frontImageRes,
        )

        assertEquals(3, result.onboardingPairs.size)
        assertEquals(6, onboardingImages.size)
        assertEquals(8, allImages.size)
        assertEquals(8, allImages.distinct().size)
    }

    @Test
    fun `같은 난수 시드에서는 같은 배정을 반환한다`() {
        val first = SeasonalDummyImageSelector.select(DummyImageSeason.Summer, Random(18))
        val second = SeasonalDummyImageSelector.select(DummyImageSeason.Summer, Random(18))

        assertEquals(first, second)
        assertTrue(first.onboardingPairs.isNotEmpty())
    }
}

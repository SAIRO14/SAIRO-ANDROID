package com.example.sairo14.data.repository

import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SavedTrip
import com.example.sairo14.domain.model.SavedTripPage
import com.example.sairo14.domain.model.SavedTripSaveResult
import com.example.sairo14.domain.repository.SavedTripRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultHomeRepositoryTest {

    @Test
    fun `홈에는 최신 저장 여행지를 최대 여덟 개 요청해 카드 요약으로 반환한다`() = runTest {
        val savedTripRepository = StubSavedTripRepository(
            result = AppResult.Success(
                SavedTripPage(
                    items = listOf(
                        savedTrip(
                            imageUrl = null,
                            spotImageUrls = listOf("", "https://example.com/spot.jpg"),
                        ),
                    ),
                    nextCursor = "next",
                ),
            ),
        )
        val repository = DefaultHomeRepository(savedTripRepository)

        val result = repository.getHomeContent() as AppResult.Success

        assertEquals(8, savedTripRepository.requestedSize)
        assertEquals(1, result.value.savedTrips.size)
        assertEquals("saved-trip-1", result.value.savedTrips.single().savedTripId)
        assertEquals("course-1", result.value.savedTrips.single().courseId)
        assertEquals("제주", result.value.savedTrips.single().regionName)
        assertEquals(
            "https://example.com/spot.jpg",
            result.value.savedTrips.single().thumbnailImageUrl,
        )
    }

    @Test
    fun `저장 여행지 조회 실패를 홈 조회 실패로 전달한다`() = runTest {
        val repository = DefaultHomeRepository(
            StubSavedTripRepository(AppResult.Failure(AppError.NetworkUnavailable)),
        )

        assertEquals(
            AppResult.Failure(AppError.NetworkUnavailable),
            repository.getHomeContent(),
        )
    }

    private class StubSavedTripRepository(
        private val result: AppResult<SavedTripPage>,
    ) : SavedTripRepository {
        var requestedSize: Int? = null

        override suspend fun saveTrip(courseId: String): AppResult<SavedTripSaveResult> =
            error("사용하지 않는 테스트 경로입니다.")

        override suspend fun getSavedTrips(
            cursor: String?,
            size: Int,
        ): AppResult<SavedTripPage> {
            requestedSize = size
            return result
        }

        override suspend fun deleteSavedTrip(savedTripId: String): AppResult<Unit> =
            error("사용하지 않는 테스트 경로입니다.")
    }

    private fun savedTrip(
        imageUrl: String?,
        spotImageUrls: List<String>,
    ) = SavedTrip(
        savedTripId = "saved-trip-1",
        courseId = "course-1",
        regionName = "제주",
        regionArea = null,
        imageUrl = imageUrl,
        reason = null,
        spotImageUrls = spotImageUrls,
        createdAt = "2026-08-18T00:00:00Z",
    )
}

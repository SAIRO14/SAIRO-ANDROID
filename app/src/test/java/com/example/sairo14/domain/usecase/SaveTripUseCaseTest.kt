package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SavedTripPage
import com.example.sairo14.domain.model.SavedTripSaveResult
import com.example.sairo14.domain.repository.SavedTripRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveTripUseCaseTest {

    @Test
    fun `저장할 코스 ID를 Repository에 전달하고 저장 결과를 반환한다`() = runTest {
        val repository = RecordingSavedTripRepository(
            result = AppResult.Success(
                SavedTripSaveResult(
                    savedTripId = "saved-trip-1",
                    courseId = "course-1",
                ),
            ),
        )
        val useCase = SaveTripUseCase(repository)

        val result = useCase("course-1")

        assertEquals("course-1", repository.savedCourseId)
        assertEquals(
            AppResult.Success(SavedTripSaveResult("saved-trip-1", "course-1")),
            result,
        )
    }

    private class RecordingSavedTripRepository(
        private val result: AppResult<SavedTripSaveResult>,
    ) : SavedTripRepository {
        var savedCourseId: String? = null
            private set

        override suspend fun saveTrip(courseId: String): AppResult<SavedTripSaveResult> {
            savedCourseId = courseId
            return result
        }

        override suspend fun getSavedTrips(
            cursor: String?,
            size: Int,
        ): AppResult<SavedTripPage> = AppResult.Success(SavedTripPage(emptyList(), null))

        override suspend fun deleteSavedTrip(savedTripId: String): AppResult<Unit> = AppResult.Success(Unit)
    }
}

package com.example.sairo14.data.repository

import com.example.sairo14.data.remote.SairoApi
import com.example.sairo14.data.remote.dto.CourseResponseDto
import com.example.sairo14.data.remote.dto.PhotoResponseDto
import com.example.sairo14.data.remote.dto.SavedTripListResponseDto
import com.example.sairo14.data.remote.dto.SavedTripSaveRequestDto
import com.example.sairo14.data.remote.dto.SavedTripSaveResponseDto
import com.example.sairo14.data.remote.dto.ShareCourseResponseDto
import com.example.sairo14.data.remote.dto.SharedCourseResponseDto
import com.example.sairo14.data.remote.dto.TasteAnalysisRequestDto
import com.example.sairo14.data.remote.dto.TasteAnalysisResponseDto
import com.example.sairo14.data.repository.remote.RemotePhotoSelectionRepository
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import java.io.IOException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemotePhotoSelectionRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `사진 응답을 도메인 후보 목록으로 변환한다`() = runTest {
        val api = RecordingSairoApi(
            photos = listOf(
                PhotoResponseDto(
                    id = "photo-1",
                    imageUrl = "https://example.com/photo-1.jpg",
                ),
            ),
        )
        val repository = RemotePhotoSelectionRepository(api, json)

        val result = repository.getPhotoCandidates(limit = 40)

        val photos = result.successValue()
        assertEquals(40, api.requestedLimit)
        assertEquals("photo-1", photos.single().id)
        assertEquals("https://example.com/photo-1.jpg", photos.single().imageUrl)
        assertEquals(null, photos.single().contentDescription)
    }

    @Test
    fun `네트워크 실패를 재시도 가능한 앱 오류로 변환한다`() = runTest {
        val repository = RemotePhotoSelectionRepository(
            api = RecordingSairoApi(error = IOException("offline")),
            json = json,
        )

        val result = repository.getPhotoCandidates(limit = 40)

        assertTrue(
            result is AppResult.Failure && result.error is AppError.NetworkUnavailable,
        )
    }

    private fun <T> AppResult<T>.successValue(): T =
        (this as? AppResult.Success<T>)?.value
            ?: error("성공 결과를 기대했습니다.")

    private class RecordingSairoApi(
        private val photos: List<PhotoResponseDto> = emptyList(),
        private val error: Throwable? = null,
    ) : SairoApi {
        var requestedLimit: Int? = null
            private set

        override suspend fun getCourse(
            courseId: String,
            deviceId: String,
        ): CourseResponseDto = error("호출되지 않아야 합니다.")

        override suspend fun shareCourse(
            courseId: String,
            deviceId: String,
        ): ShareCourseResponseDto = error("호출되지 않아야 합니다.")

        override suspend fun getSharedCourse(shareId: String): SharedCourseResponseDto =
            error("호출되지 않아야 합니다.")

        override suspend fun saveTrip(
            deviceId: String,
            request: SavedTripSaveRequestDto,
        ): SavedTripSaveResponseDto = error("호출되지 않아야 합니다.")

        override suspend fun deleteSavedTrip(deviceId: String, savedTripId: String) =
            error("호출되지 않아야 합니다.")

        override suspend fun getSavedTrips(
            deviceId: String,
            cursor: String?,
            size: Int,
        ): SavedTripListResponseDto = error("호출되지 않아야 합니다.")

        override suspend fun getPhotos(limit: Int): List<PhotoResponseDto> {
            requestedLimit = limit
            error?.let { throwable -> throw throwable }
            return photos
        }

        override suspend fun analyzeTaste(
            deviceId: String,
            request: TasteAnalysisRequestDto,
        ): TasteAnalysisResponseDto = error("취향 분석 API는 이 테스트에서 호출하지 않습니다.")
    }
}

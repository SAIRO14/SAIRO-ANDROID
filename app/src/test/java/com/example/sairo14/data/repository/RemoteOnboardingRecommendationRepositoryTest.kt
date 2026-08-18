package com.example.sairo14.data.repository

import androidx.datastore.core.CorruptionException
import com.example.sairo14.core.datastore.DeviceIdProvider
import com.example.sairo14.data.remote.SairoApi
import com.example.sairo14.data.remote.dto.CourseCardDto
import com.example.sairo14.data.remote.dto.CourseResponseDto
import com.example.sairo14.data.remote.dto.PhotoResponseDto
import com.example.sairo14.data.remote.dto.SavedTripListResponseDto
import com.example.sairo14.data.remote.dto.SavedTripSaveRequestDto
import com.example.sairo14.data.remote.dto.SavedTripSaveResponseDto
import com.example.sairo14.data.remote.dto.TasteAnalysisRequestDto
import com.example.sairo14.data.remote.dto.TasteAnalysisResponseDto
import com.example.sairo14.data.repository.remote.RemoteOnboardingRecommendationRepository
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteOnboardingRecommendationRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `고유 사진 ID와 기기 ID를 전달하고 분석 결과를 도메인 모델로 변환한다`() = runTest {
        val api = RecordingSairoApi(response = tasteAnalysisResponse)
        val repository = RemoteOnboardingRecommendationRepository(
            api = api,
            deviceIdProvider = TestDeviceIdProvider("device-1"),
            json = json,
        )

        val result = repository.analyzeTaste(
            selectedPhotoIds = listOf("photo-1", "photo-2", "photo-1", "photo-3", "photo-4", "photo-5"),
        )

        val analysis = result.successValue()
        assertEquals("device-1", api.requestedDeviceId)
        assertEquals(
            listOf("photo-1", "photo-2", "photo-3", "photo-4", "photo-5"),
            api.requestedPhotoIds,
        )
        assertEquals(listOf("고요한"), analysis.moodTags)
        assertEquals("course-1", analysis.recommendations.single().courseId)
        assertTrue("course-1" in analysis.courses)
    }

    @Test
    fun `고유 사진이 다섯 장보다 적으면 서버를 호출하지 않는다`() = runTest {
        val api = RecordingSairoApi(response = tasteAnalysisResponse)
        val repository = RemoteOnboardingRecommendationRepository(
            api = api,
            deviceIdProvider = TestDeviceIdProvider("device-1"),
            json = json,
        )

        val result = repository.analyzeTaste(
            selectedPhotoIds = listOf("photo-1", "photo-1", "photo-2", "photo-3", "photo-4"),
        )

        assertEquals(AppResult.Failure(AppError.InvalidRequest), result)
        assertEquals(null, api.requestedPhotoIds)
    }

    @Test
    fun `기기 식별자 저장소 오류를 네트워크 오류로 바꾸지 않는다`() = runTest {
        val repository = RemoteOnboardingRecommendationRepository(
            api = RecordingSairoApi(response = tasteAnalysisResponse),
            deviceIdProvider = FailingDeviceIdProvider(IOException("storage unavailable")),
            json = json,
        )

        val result = repository.analyzeTaste(selectedPhotoIds)

        assertEquals(AppResult.Failure(AppError.StorageUnavailable), result)
    }

    @Test
    fun `손상된 기기 식별자 저장소 오류를 보존한다`() = runTest {
        val repository = RemoteOnboardingRecommendationRepository(
            api = RecordingSairoApi(response = tasteAnalysisResponse),
            deviceIdProvider = FailingDeviceIdProvider(CorruptionException("corrupted")),
            json = json,
        )

        val result = repository.analyzeTaste(selectedPhotoIds)

        assertEquals(AppResult.Failure(AppError.StorageCorrupted), result)
    }

    @Test
    fun `취향 분석 네트워크 오류를 재시도 가능한 앱 오류로 변환한다`() = runTest {
        val repository = RemoteOnboardingRecommendationRepository(
            api = RecordingSairoApi(error = IOException("offline")),
            deviceIdProvider = TestDeviceIdProvider("device-1"),
            json = json,
        )

        val result = repository.analyzeTaste(selectedPhotoIds)

        assertEquals(AppResult.Failure(AppError.NetworkUnavailable), result)
    }

    @Test(expected = CancellationException::class)
    fun `취소는 결과로 변환하지 않는다`() = runTest {
        val repository = RemoteOnboardingRecommendationRepository(
            api = RecordingSairoApi(response = tasteAnalysisResponse),
            deviceIdProvider = FailingDeviceIdProvider(CancellationException()),
            json = json,
        )

        repository.analyzeTaste(selectedPhotoIds)
    }

    private fun <T> AppResult<T>.successValue(): T =
        (this as? AppResult.Success<T>)?.value ?: error("성공 결과를 기대했습니다.")

    private class TestDeviceIdProvider(
        private val deviceId: String,
    ) : DeviceIdProvider {
        override suspend fun getDeviceId(): String = deviceId
    }

    private class FailingDeviceIdProvider(
        private val error: Throwable,
    ) : DeviceIdProvider {
        override suspend fun getDeviceId(): String = throw error
    }

    private class RecordingSairoApi(
        private val response: TasteAnalysisResponseDto? = null,
        private val error: Throwable? = null,
    ) : SairoApi {
        var requestedDeviceId: String? = null
            private set
        var requestedPhotoIds: List<String>? = null
            private set

        override suspend fun getCourse(
            courseId: String,
            deviceId: String,
        ): CourseResponseDto = error("호출되지 않아야 합니다.")

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

        override suspend fun getPhotos(limit: Int): List<PhotoResponseDto> = emptyList()

        override suspend fun analyzeTaste(
            deviceId: String,
            request: TasteAnalysisRequestDto,
        ): TasteAnalysisResponseDto {
            requestedDeviceId = deviceId
            requestedPhotoIds = request.photoIds
            error?.let { throwable -> throw throwable }
            return checkNotNull(response)
        }
    }

    private companion object {
        val selectedPhotoIds = listOf("photo-1", "photo-2", "photo-3", "photo-4", "photo-5")
        val tasteAnalysisResponse = TasteAnalysisResponseDto(
            moodTags = listOf("고요한"),
            summary = "자연 속에서 여유를 즐기는 취향이에요.",
            courses = listOf(
                CourseCardDto(
                    courseId = "course-1",
                    regionName = "제주도",
                    saved = false,
                    day1 = emptyList(),
                    day2 = emptyList(),
                ),
            ),
        )
    }
}

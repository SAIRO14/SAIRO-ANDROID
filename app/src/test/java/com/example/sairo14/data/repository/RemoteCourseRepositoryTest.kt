package com.example.sairo14.data.repository

import androidx.datastore.core.CorruptionException
import com.example.sairo14.core.datastore.DeviceIdProvider
import com.example.sairo14.data.remote.SairoApi
import com.example.sairo14.data.remote.dto.CourseResponseDto
import com.example.sairo14.data.remote.dto.PhotoResponseDto
import com.example.sairo14.data.remote.dto.SavedTripListResponseDto
import com.example.sairo14.data.remote.dto.SavedTripSaveRequestDto
import com.example.sairo14.data.remote.dto.SavedTripSaveResponseDto
import com.example.sairo14.data.remote.dto.SpotSummaryDto
import com.example.sairo14.data.remote.dto.TasteAnalysisRequestDto
import com.example.sairo14.data.remote.dto.TasteAnalysisResponseDto
import com.example.sairo14.data.repository.remote.RemoteCourseRepository
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.model.CourseDay
import com.example.sairo14.domain.model.CoursePlace
import com.example.sairo14.domain.model.MapCoordinate
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteCourseRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `코스 조회 요청에 기기 ID와 코스 ID를 전달하고 domain 코스를 반환한다`() = runTest {
        val api = RecordingSairoApi()
        val repository = createRepository(api = api)

        val result = repository.getCourse("course-1")

        assertEquals("course-1", api.requestedCourseId)
        assertEquals("device-1", api.requestedDeviceId)
        assertEquals(
            AppResult.Success(
                Course(
                    courseId = "course-1",
                    regionName = "제주도",
                    days = listOf(
                        CourseDay(
                            dayNumber = 1,
                            places = listOf(
                                CoursePlace(
                                    placeId = "spot-1",
                                    name = "성산일출봉",
                                    imageUrl = "https://example.com/spot.jpg",
                                    tags = listOf("09:00~18:00", "가능"),
                                    coordinate = MapCoordinate(33.0, 126.0),
                                    operatingHours = "09:00~18:00",
                                    parking = "가능",
                                ),
                            ),
                        ),
                        CourseDay(dayNumber = 2, places = emptyList()),
                    ),
                    isSaved = true,
                ),
            ),
            result,
        )
    }

    @Test
    fun `코스 조회 네트워크 오류를 재시도 가능한 앱 오류로 변환한다`() = runTest {
        val repository = createRepository(
            api = RecordingSairoApi(courseError = IOException("offline")),
        )

        assertEquals(
            AppResult.Failure(AppError.NetworkUnavailable),
            repository.getCourse("course-1"),
        )
    }

    @Test
    fun `기기 식별자 저장소 손상을 그대로 반환한다`() = runTest {
        val repository = RemoteCourseRepository(
            api = RecordingSairoApi(),
            deviceIdProvider = FailingDeviceIdProvider(CorruptionException("corrupted")),
            json = json,
        )

        assertEquals(
            AppResult.Failure(AppError.StorageCorrupted),
            repository.getCourse("course-1"),
        )
    }

    @Test(expected = CancellationException::class)
    fun `기기 식별자 조회 취소는 결과로 변환하지 않는다`() = runTest {
        val repository = RemoteCourseRepository(
            api = RecordingSairoApi(),
            deviceIdProvider = FailingDeviceIdProvider(CancellationException()),
            json = json,
        )

        repository.getCourse("course-1")
    }

    private fun createRepository(api: SairoApi) = RemoteCourseRepository(
        api = api,
        deviceIdProvider = TestDeviceIdProvider("device-1"),
        json = json,
    )

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
        private val courseResponse: CourseResponseDto = courseResponse(),
        private val courseError: Throwable? = null,
    ) : SairoApi {
        var requestedCourseId: String? = null
            private set
        var requestedDeviceId: String? = null
            private set

        override suspend fun getCourse(
            courseId: String,
            deviceId: String,
        ): CourseResponseDto {
            requestedCourseId = courseId
            requestedDeviceId = deviceId
            courseError?.let { throwable -> throw throwable }
            return courseResponse
        }

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

        override suspend fun getPhotos(limit: Int): List<PhotoResponseDto> =
            error("호출되지 않아야 합니다.")

        override suspend fun analyzeTaste(
            deviceId: String,
            request: TasteAnalysisRequestDto,
        ): TasteAnalysisResponseDto = error("호출되지 않아야 합니다.")
    }

    private companion object {
        fun courseResponse() = CourseResponseDto(
            courseId = "course-1",
            regionName = "제주도",
            saved = true,
            day1 = listOf(
                SpotSummaryDto(
                    spotId = "spot-1",
                    name = "성산일출봉",
                    lat = 33.0,
                    lng = 126.0,
                    imageUrl = "https://example.com/spot.jpg",
                    operatingHours = "09:00~18:00",
                    parking = "가능",
                ),
            ),
            day2 = emptyList(),
        )
    }
}

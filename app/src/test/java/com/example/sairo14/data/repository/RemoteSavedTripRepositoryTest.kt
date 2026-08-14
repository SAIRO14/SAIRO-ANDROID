package com.example.sairo14.data.repository

import androidx.datastore.core.CorruptionException
import com.example.sairo14.core.datastore.DeviceIdProvider
import com.example.sairo14.data.remote.SairoApi
import com.example.sairo14.data.remote.dto.PhotoResponseDto
import com.example.sairo14.data.remote.dto.SavedTripSaveRequestDto
import com.example.sairo14.data.remote.dto.SavedTripSaveResponseDto
import com.example.sairo14.data.remote.dto.TasteAnalysisRequestDto
import com.example.sairo14.data.remote.dto.TasteAnalysisResponseDto
import com.example.sairo14.data.repository.remote.RemoteSavedTripRepository
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SavedTripSaveResult
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteSavedTripRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `저장 요청에 기기 ID와 코스 ID를 전달하고 저장 식별자를 반환한다`() = runTest {
        val api = RecordingSairoApi()
        val repository = createRepository(api = api)

        val result = repository.saveTrip("course-1")

        assertEquals("device-1", api.savedDeviceId)
        assertEquals("course-1", api.savedRequest?.courseId)
        assertEquals(
            AppResult.Success(SavedTripSaveResult("saved-trip-1", "course-1")),
            result,
        )
    }

    @Test
    fun `삭제 요청에 기기 ID와 저장 항목 ID를 전달하고 성공으로 처리한다`() = runTest {
        val api = RecordingSairoApi()
        val repository = createRepository(api = api)

        val result = repository.deleteSavedTrip("saved-trip-1")

        assertEquals("device-1", api.deletedDeviceId)
        assertEquals("saved-trip-1", api.deletedSavedTripId)
        assertEquals(AppResult.Success(Unit), result)
    }

    @Test
    fun `기기 식별자 저장소 오류를 네트워크 오류로 바꾸지 않는다`() = runTest {
        val repository = RemoteSavedTripRepository(
            api = RecordingSairoApi(),
            deviceIdProvider = FailingDeviceIdProvider(IOException("storage unavailable")),
            json = json,
        )

        assertEquals(
            AppResult.Failure(AppError.StorageUnavailable),
            repository.saveTrip("course-1"),
        )
    }

    @Test
    fun `저장 네트워크 오류를 재시도 가능한 앱 오류로 변환한다`() = runTest {
        val repository = createRepository(api = RecordingSairoApi(saveError = IOException("offline")))

        assertEquals(
            AppResult.Failure(AppError.NetworkUnavailable),
            repository.saveTrip("course-1"),
        )
    }

    @Test(expected = CancellationException::class)
    fun `취소는 결과로 변환하지 않는다`() = runTest {
        val repository = RemoteSavedTripRepository(
            api = RecordingSairoApi(),
            deviceIdProvider = FailingDeviceIdProvider(CancellationException()),
            json = json,
        )

        repository.deleteSavedTrip("saved-trip-1")
    }

    private fun createRepository(api: SairoApi) = RemoteSavedTripRepository(
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
        private val saveError: Throwable? = null,
    ) : SairoApi {
        var savedDeviceId: String? = null
            private set
        var savedRequest: SavedTripSaveRequestDto? = null
            private set
        var deletedDeviceId: String? = null
            private set
        var deletedSavedTripId: String? = null
            private set

        override suspend fun saveTrip(
            deviceId: String,
            request: SavedTripSaveRequestDto,
        ): SavedTripSaveResponseDto {
            savedDeviceId = deviceId
            savedRequest = request
            saveError?.let { throwable -> throw throwable }
            return SavedTripSaveResponseDto(
                savedTripId = "saved-trip-1",
                courseId = "course-1",
            )
        }

        override suspend fun deleteSavedTrip(deviceId: String, savedTripId: String) {
            deletedDeviceId = deviceId
            deletedSavedTripId = savedTripId
        }

        override suspend fun getPhotos(limit: Int): List<PhotoResponseDto> = emptyList()

        override suspend fun analyzeTaste(
            deviceId: String,
            request: TasteAnalysisRequestDto,
        ): TasteAnalysisResponseDto = error("호출되지 않아야 합니다.")
    }
}

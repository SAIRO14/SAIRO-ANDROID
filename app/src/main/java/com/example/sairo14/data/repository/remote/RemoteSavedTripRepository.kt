package com.example.sairo14.data.repository.remote

import androidx.datastore.core.CorruptionException
import com.example.sairo14.core.datastore.DeviceIdProvider
import com.example.sairo14.data.mapper.toDomain
import com.example.sairo14.data.remote.SairoApi
import com.example.sairo14.data.remote.dto.SavedTripSaveRequestDto
import com.example.sairo14.data.remote.runRemoteOperation
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SavedTripPage
import com.example.sairo14.domain.model.SavedTripSaveResult
import com.example.sairo14.domain.repository.SavedTripRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import timber.log.Timber

/** SAIRO 저장 여행지 API를 저장·조회·해제 Domain 계약으로 제공한다. */
@Singleton
class RemoteSavedTripRepository @Inject constructor(
    private val api: SairoApi,
    private val deviceIdProvider: DeviceIdProvider,
    private val json: Json,
) : SavedTripRepository {

    override suspend fun saveTrip(courseId: String): AppResult<SavedTripSaveResult> {
        val deviceId = when (val result = getDeviceId()) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }

        return runRemoteOperation(
            action = "여행지를 저장하지 못했습니다.",
            json = json,
        ) {
            api.saveTrip(
                deviceId = deviceId,
                request = SavedTripSaveRequestDto(courseId = courseId),
            ).toDomain()
        }
    }

    override suspend fun deleteSavedTrip(savedTripId: String): AppResult<Unit> {
        val deviceId = when (val result = getDeviceId()) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }

        return runRemoteOperation(
            action = "저장한 여행지를 해제하지 못했습니다.",
            json = json,
        ) {
            api.deleteSavedTrip(
                deviceId = deviceId,
                savedTripId = savedTripId,
            )
        }
    }

    override suspend fun getSavedTrips(
        cursor: String?,
        size: Int,
    ): AppResult<SavedTripPage> {
        val deviceId = when (val result = getDeviceId()) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }

        return runRemoteOperation(
            action = "저장한 여행지를 불러오지 못했습니다.",
            json = json,
        ) {
            api.getSavedTrips(
                deviceId = deviceId,
                cursor = cursor,
                size = size,
            ).toDomain()
        }
    }

    private suspend fun getDeviceId(): AppResult<String> = try {
        AppResult.Success(deviceIdProvider.getDeviceId())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (exception: CorruptionException) {
        Timber.e(exception, "저장 여행지용 기기 식별자를 읽지 못했습니다.")
        AppResult.Failure(AppError.StorageCorrupted)
    } catch (exception: IOException) {
        Timber.e(exception, "저장 여행지용 기기 식별자를 읽지 못했습니다.")
        AppResult.Failure(AppError.StorageUnavailable)
    } catch (exception: Exception) {
        Timber.e(exception, "저장 여행지용 기기 식별자를 읽지 못했습니다.")
        AppResult.Failure(AppError.Unknown)
    }
}

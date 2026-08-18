package com.example.sairo14.data.repository.remote

import androidx.datastore.core.CorruptionException
import com.example.sairo14.core.datastore.DeviceIdProvider
import com.example.sairo14.data.mapper.toDomain
import com.example.sairo14.data.remote.SairoApi
import com.example.sairo14.data.remote.dto.TasteAnalysisRequestDto
import com.example.sairo14.data.remote.runRemoteOperation
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.repository.OnboardingRecommendationRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import timber.log.Timber

/** SAIRO 취향 분석 API를 온보딩 추천 Domain 계약으로 제공한다. */
@Singleton
class RemoteOnboardingRecommendationRepository @Inject constructor(
    private val api: SairoApi,
    private val deviceIdProvider: DeviceIdProvider,
    private val json: Json,
) : OnboardingRecommendationRepository {

    override suspend fun analyzeTaste(
        selectedPhotoIds: List<String>,
    ): AppResult<OnboardingAnalysisResult> {
        val photoIds = selectedPhotoIds.toDistinctPhotoIdsOrNull()
            ?: return AppResult.Failure(AppError.InvalidRequest)
        val deviceId = when (val result = getDeviceId()) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }

        return runRemoteOperation(
            action = "온보딩 취향 분석 결과를 불러오지 못했습니다.",
            json = json,
        ) {
            api.analyzeTaste(
                deviceId = deviceId,
                request = TasteAnalysisRequestDto(photoIds = photoIds),
            ).toDomain()
        }
    }

    private suspend fun getDeviceId(): AppResult<String> = try {
        AppResult.Success(deviceIdProvider.getDeviceId())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (exception: CorruptionException) {
        Timber.e(exception, "온보딩 취향 분석용 기기 식별자를 읽지 못했습니다.")
        AppResult.Failure(AppError.StorageCorrupted)
    } catch (exception: IOException) {
        Timber.e(exception, "온보딩 취향 분석용 기기 식별자를 읽지 못했습니다.")
        AppResult.Failure(AppError.StorageUnavailable)
    } catch (exception: Exception) {
        Timber.e(exception, "온보딩 취향 분석용 기기 식별자를 읽지 못했습니다.")
        AppResult.Failure(AppError.Unknown)
    }
}

private fun List<String>.toDistinctPhotoIdsOrNull(): List<String>? {
    if (any { id -> id.isBlank() }) return null

    val photoIds = distinct()
    return photoIds.takeIf { ids -> ids.size in MinimumPhotoCount..MaximumPhotoCount }
}

private const val MinimumPhotoCount = 5
private const val MaximumPhotoCount = 10

package com.example.sairo14.data.repository.remote

import androidx.datastore.core.CorruptionException
import com.example.sairo14.core.datastore.DeviceIdProvider
import com.example.sairo14.data.mapper.toDomain
import com.example.sairo14.data.remote.SairoApi
import com.example.sairo14.data.remote.runRemoteOperation
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.repository.CourseRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import timber.log.Timber

/** SAIRO 코스 조회 API를 여행 상세 화면용 Domain 계약으로 제공한다. */
@Singleton
class RemoteCourseRepository @Inject constructor(
    private val api: SairoApi,
    private val deviceIdProvider: DeviceIdProvider,
    private val json: Json,
) : CourseRepository {

    override suspend fun getCourse(courseId: String): AppResult<Course> {
        val deviceId = when (val result = getDeviceId()) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }

        return runRemoteOperation(
            action = "코스 상세 정보를 불러오지 못했습니다.",
            json = json,
        ) {
            api.getCourse(
                courseId = courseId,
                deviceId = deviceId,
            ).toDomain()
        }
    }

    private suspend fun getDeviceId(): AppResult<String> = try {
        AppResult.Success(deviceIdProvider.getDeviceId())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (exception: CorruptionException) {
        Timber.e(exception, "코스 조회용 기기 식별자를 읽지 못했습니다.")
        AppResult.Failure(AppError.StorageCorrupted)
    } catch (exception: IOException) {
        Timber.e(exception, "코스 조회용 기기 식별자를 읽지 못했습니다.")
        AppResult.Failure(AppError.StorageUnavailable)
    } catch (exception: Exception) {
        Timber.e(exception, "코스 조회용 기기 식별자를 읽지 못했습니다.")
        AppResult.Failure(AppError.Unknown)
    }
}

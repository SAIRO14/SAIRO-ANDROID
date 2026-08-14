package com.example.sairo14.data.repository.fake

import com.example.sairo14.core.datastore.DeviceIdProvider
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.SavedTrip
import com.example.sairo14.domain.model.SavedTripPage
import com.example.sairo14.domain.model.SavedTripSaveResult
import com.example.sairo14.domain.repository.SavedTripRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** 서버 연동 전 저장·조회·삭제 흐름을 확인할 수 있도록 기기별 인메모리 여행지 데이터를 제공한다. */
@Singleton
class FakeSavedTripRepository @Inject constructor(
    private val deviceIdProvider: DeviceIdProvider,
) : SavedTripRepository {
    private val mutex = Mutex()
    private val savedTripsByDeviceId = mutableMapOf<String, List<SavedTrip>>()

    override suspend fun saveTrip(courseId: String): AppResult<SavedTripSaveResult> = mutex.withLock {
        val deviceId = deviceIdProvider.getDeviceId()
        val currentTrips = savedTripsByDeviceId.getOrPut(deviceId) { sampleSavedTrips }
        val existingTrip = currentTrips.firstOrNull { trip -> trip.courseId == courseId }
        val savedTrip = existingTrip ?: SavedTrip(
            savedTripId = "saved-trip-$courseId",
            courseId = courseId,
            regionName = "새로 저장한 여행지",
            regionArea = null,
            imageUrl = null,
            reason = "저장 API 계약 검증용 여행지",
            createdAt = "2026-08-14T00:00:00Z",
        ).also { newTrip ->
            savedTripsByDeviceId[deviceId] = listOf(newTrip) + currentTrips
        }

        AppResult.Success(
            SavedTripSaveResult(
                savedTripId = savedTrip.savedTripId,
                courseId = savedTrip.courseId,
            ),
        )
    }

    override suspend fun getSavedTrips(
        cursor: String?,
        size: Int,
    ): AppResult<SavedTripPage> = mutex.withLock {
        val deviceId = deviceIdProvider.getDeviceId()
        if (size !in MinPageSize..MaxPageSize) {
            return@withLock AppResult.Failure(AppError.InvalidRequest)
        }

        val currentTrips = savedTripsByDeviceId.getOrPut(deviceId) { sampleSavedTrips }
        val startIndex = when (cursor) {
            null -> 0
            else -> cursor.toStartIndexOrNull()
                ?: return@withLock AppResult.Failure(AppError.InvalidCursor)
        }
        if (cursor != null && startIndex !in currentTrips.indices) {
            return@withLock AppResult.Failure(AppError.InvalidCursor)
        }

        val endIndexExclusive = (startIndex + size).coerceAtMost(currentTrips.size)
        AppResult.Success(
            SavedTripPage(
                items = currentTrips.subList(startIndex, endIndexExclusive).toList(),
                nextCursor = endIndexExclusive.takeIf { it < currentTrips.size }
                    ?.let(::toCursor),
            ),
        )
    }

    override suspend fun deleteSavedTrip(savedTripId: String): AppResult<Unit> = mutex.withLock {
        val deviceId = deviceIdProvider.getDeviceId()
        val currentTrips = savedTripsByDeviceId.getOrPut(deviceId) { sampleSavedTrips }
        val updatedTrips = currentTrips.filterNot { trip -> trip.savedTripId == savedTripId }

        if (updatedTrips.size == currentTrips.size) {
            AppResult.Failure(AppError.Unknown)
        } else {
            savedTripsByDeviceId[deviceId] = updatedTrips
            AppResult.Success(Unit)
        }
    }

    private companion object {
        const val MinPageSize = 1
        const val MaxPageSize = 50
        const val CursorPrefix = "fake-saved-trip-cursor-"

        fun toCursor(startIndex: Int): String = "$CursorPrefix$startIndex"

        val sampleSavedTrips = listOf(
            SavedTrip(
                savedTripId = "saved-trip-boeun",
                courseId = "course-boeun",
                regionName = "충북 보은권",
                regionArea = "보은군",
                imageUrl = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=900&q=85",
                reason = "고요한 자연과 전통의 분위기",
                createdAt = "2026-08-14T10:00:00Z",
            ),
            SavedTrip(
                savedTripId = "saved-trip-gangneung",
                courseId = "course-gangneung",
                regionName = "강원 강릉권",
                regionArea = "강릉시",
                imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=900&q=85",
                reason = "바다와 골목이 어우러진 느긋한 풍경",
                createdAt = "2026-08-13T10:00:00Z",
            ),
            SavedTrip(
                savedTripId = "saved-trip-jeju",
                courseId = "course-jeju",
                regionName = "제주 서부권",
                regionArea = "제주시",
                imageUrl = "https://images.unsplash.com/photo-1439853949127-fa647821eba0?auto=format&fit=crop&w=900&q=85",
                reason = "빛과 바람을 따라 걷는 한적한 하루",
                createdAt = "2026-08-12T10:00:00Z",
            ),
        )
    }

    private fun String.toStartIndexOrNull(): Int? =
        removePrefix(CursorPrefix)
            .takeIf { startsWith(CursorPrefix) }
            ?.toIntOrNull()
}

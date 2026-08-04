package com.example.sairo14.data.repository

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.SavedTrip
import com.example.sairo14.domain.repository.SavedTripRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** 서버 연동 전 저장 목록 조회와 삭제를 확인할 수 있도록 인메모리 여행지 데이터를 제공한다. */
@Singleton
class FakeSavedTripRepository @Inject constructor() : SavedTripRepository {
    private val mutex = Mutex()
    private val savedTripsByDeviceId = mutableMapOf<String, List<SavedTrip>>()

    override suspend fun getSavedTrips(deviceId: String): AppResult<List<SavedTrip>> = mutex.withLock {
        AppResult.Success(savedTripsByDeviceId.getOrPut(deviceId) { sampleSavedTrips })
    }

    override suspend fun deleteSavedTrip(
        deviceId: String,
        savedTripId: String,
    ): AppResult<Unit> = mutex.withLock {
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
        val sampleSavedTrips = listOf(
            SavedTrip(
                savedTripId = "saved-trip-boeun",
                courseId = "course-boeun",
                regionName = "충북 보은권",
                description = "고요한 자연과 전통의 분위기",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=900&q=85",
                    "https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=900&q=85",
                ),
                placeNames = listOf("말티재 전망대", "세조길 숲 산책"),
            ),
            SavedTrip(
                savedTripId = "saved-trip-gangneung",
                courseId = "course-gangneung",
                regionName = "강원 강릉권",
                description = "바다와 골목이 어우러진 느긋한 풍경",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=900&q=85",
                    "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=900&q=85",
                ),
                placeNames = listOf("안목해변", "명주동 골목"),
            ),
            SavedTrip(
                savedTripId = "saved-trip-jeju",
                courseId = "course-jeju",
                regionName = "제주 서부권",
                description = "빛과 바람을 따라 걷는 한적한 하루",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1439853949127-fa647821eba0?auto=format&fit=crop&w=900&q=85",
                    "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=900&q=85",
                ),
                placeNames = listOf("협재해수욕장", "금능해안"),
            ),
        )
    }
}

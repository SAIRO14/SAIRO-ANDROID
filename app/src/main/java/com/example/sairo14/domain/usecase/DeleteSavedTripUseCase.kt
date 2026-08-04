package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.repository.SavedTripRepository
import javax.inject.Inject

/** 저장된 여행지 한 건의 북마크를 해제한다. */
class DeleteSavedTripUseCase @Inject constructor(
    private val savedTripRepository: SavedTripRepository,
) {
    /** 익명 사용자 식별자에 연결된 저장 여행지를 삭제한다. */
    suspend operator fun invoke(
        deviceId: String,
        savedTripId: String,
    ): AppResult<Unit> = savedTripRepository.deleteSavedTrip(deviceId, savedTripId)
}

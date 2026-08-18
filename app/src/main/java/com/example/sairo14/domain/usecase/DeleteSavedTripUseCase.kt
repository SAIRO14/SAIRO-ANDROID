package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.repository.SavedTripRepository
import javax.inject.Inject

/** 저장된 여행지 한 건의 북마크를 해제한다. */
class DeleteSavedTripUseCase @Inject constructor(
    private val savedTripRepository: SavedTripRepository,
) {
    /** 현재 기기에 연결된 저장 여행지를 삭제한다.
     * @param savedTripId 저장 해제할 여행지의 안정적인 ID
     */
    suspend operator fun invoke(savedTripId: String): AppResult<Unit> =
        savedTripRepository.deleteSavedTrip(savedTripId)
}

package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SavedTripSaveResult
import com.example.sairo14.domain.repository.SavedTripRepository
import javax.inject.Inject

/** 지정한 코스를 현재 기기의 저장 여행지에 추가한다. */
class SaveTripUseCase @Inject constructor(
    private val savedTripRepository: SavedTripRepository,
) {
    /** 저장에 성공하면 이후 해제에 필요한 저장 항목 ID를 반환한다.
     * @param courseId 저장할 코스의 안정적인 ID
     */
    suspend operator fun invoke(courseId: String): AppResult<SavedTripSaveResult> =
        savedTripRepository.saveTrip(courseId)
}

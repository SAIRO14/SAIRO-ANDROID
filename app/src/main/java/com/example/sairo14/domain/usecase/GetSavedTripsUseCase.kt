package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SavedTrip
import com.example.sairo14.domain.repository.SavedTripRepository
import javax.inject.Inject

/** 현재 기기에 저장된 여행지 목록을 조회한다. */
class GetSavedTripsUseCase @Inject constructor(
    private val savedTripRepository: SavedTripRepository,
) {
    /** 저장 여행지를 최신순으로 반환한다. */
    suspend operator fun invoke(): AppResult<List<SavedTrip>> = savedTripRepository.getSavedTrips()
}

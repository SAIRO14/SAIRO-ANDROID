package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SavedTripPage
import com.example.sairo14.domain.repository.DefaultSavedTripPageSize
import com.example.sairo14.domain.repository.SavedTripRepository
import javax.inject.Inject

/** 현재 기기에 저장된 여행지 목록을 조회한다. */
class GetSavedTripsUseCase @Inject constructor(
    private val savedTripRepository: SavedTripRepository,
) {
    /** 저장 여행지 목록 한 페이지를 최신 저장순으로 반환한다.
     *
     * [cursor]는 서버 응답에서 받은 값을 그대로 전달하며, 기기 식별자 준비는 Repository 구현이 담당한다.
     * @param cursor 다음 페이지 조회에 사용할 서버 제공 커서. 첫 페이지면 `null`
     * @param size 한 번에 조회할 항목 수
     */
    suspend operator fun invoke(
        cursor: String? = null,
        size: Int = DefaultSavedTripPageSize,
    ): AppResult<SavedTripPage> = savedTripRepository.getSavedTrips(cursor, size)
}

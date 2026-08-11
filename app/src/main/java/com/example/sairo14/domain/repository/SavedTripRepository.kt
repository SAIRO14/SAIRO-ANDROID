package com.example.sairo14.domain.repository

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SavedTrip

/** 현재 기기에 저장된 여행지 목록을 최신 저장 순서로 조회하는 도메인 계약이다. */
interface SavedTripRepository {

    /** 현재 기기의 저장 여행지 목록을 한 번 조회한다. */
    suspend fun getSavedTrips(): AppResult<List<SavedTrip>>

    /** 현재 기기에서 지정한 여행지의 저장을 해제한다.
     *
     * 기기 식별은 Data 계층 구현이 관리하므로, 호출자는 [savedTripId]만 전달한다.
     * @param savedTripId 저장 해제할 여행지의 안정적인 ID
     */
    suspend fun deleteSavedTrip(savedTripId: String): AppResult<Unit>
}

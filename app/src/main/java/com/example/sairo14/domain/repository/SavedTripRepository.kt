package com.example.sairo14.domain.repository

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SavedTrip

/** 현재 익명 사용자가 저장한 여행지 목록을 최신 저장 순서로 조회하는 도메인 계약이다. */
interface SavedTripRepository {

    /** 지정한 익명 사용자의 저장 여행지 목록을 한 번 조회한다. */
    suspend fun getSavedTrips(deviceId: String): AppResult<List<SavedTrip>>

    /** 지정한 여행지의 저장을 해제한다. */
    suspend fun deleteSavedTrip(
        deviceId: String,
        savedTripId: String,
    ): AppResult<Unit>
}

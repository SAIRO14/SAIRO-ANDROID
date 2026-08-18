package com.example.sairo14.domain.repository

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SavedTripPage
import com.example.sairo14.domain.model.SavedTripSaveResult

/** 현재 기기에 저장된 여행지 목록을 최신 저장 순서로 조회하는 도메인 계약이다. */
interface SavedTripRepository {

    /** 현재 기기에서 지정한 코스를 저장하고, 이후 해제에 필요한 저장 항목 ID를 반환한다.
     *
     * 기기 식별은 Data 계층 구현이 관리하므로, 호출자는 [courseId]만 전달한다.
     * @param courseId 저장할 코스의 안정적인 ID
     */
    suspend fun saveTrip(courseId: String): AppResult<SavedTripSaveResult>

    /** 현재 기기의 저장 여행지 목록 한 페이지를 최신 저장 순서로 조회한다.
     *
     * 커서의 생성과 해석은 서버가 소유한다. 호출자는 응답의 커서를 수정하지 않고 다음 호출에 전달한다.
     * @param cursor 다음 페이지 조회에 사용할 서버 제공 커서. 첫 페이지면 `null`
     * @param size 한 번에 조회할 항목 수. 앱은 기본값 20을 사용한다
     */
    suspend fun getSavedTrips(
        cursor: String? = null,
        size: Int = DefaultSavedTripPageSize,
    ): AppResult<SavedTripPage>

    /** 현재 기기에서 지정한 여행지의 저장을 해제한다.
     *
     * 기기 식별은 Data 계층 구현이 관리하므로, 호출자는 [savedTripId]만 전달한다.
     * @param savedTripId 저장 해제할 여행지의 안정적인 ID
     */
    suspend fun deleteSavedTrip(savedTripId: String): AppResult<Unit>
}

/** 저장 여행지 목록을 한 번에 조회하는 앱 기본 페이지 크기다. */
const val DefaultSavedTripPageSize = 20

package com.example.sairo14.data.remote.dto

import kotlinx.serialization.Serializable

/** 공개 공유 코스 조회 API가 반환하는 읽기 전용 스냅샷이다.
 *
 * 현재 기기에 종속된 저장 여부는 공개 응답에 포함하지 않는다.
 * @param courseId 공유 원본 코스를 식별하는 서버 ID
 * @param regionName 사용자에게 표시할 지역명
 * @param day1 첫째 날 방문 장소 목록
 * @param day2 둘째 날 방문 장소 목록
 */
@Serializable
data class SharedCourseResponseDto(
    val courseId: String,
    val regionName: String,
    val day1: List<SpotSummaryDto>,
    val day2: List<SpotSummaryDto>,
)

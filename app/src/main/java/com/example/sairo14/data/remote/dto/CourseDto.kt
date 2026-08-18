package com.example.sairo14.data.remote.dto

import kotlinx.serialization.Serializable

/** 코스 상세 조회 API가 반환하는 지역과 일차별 장소 스냅샷이다.
 *
 * 장소 정보는 코스 생성 시점의 값이며, 값이 없는 운영 정보는 별도 장소 상세 API로 자동 보완하지 않는다.
 * @param courseId 조회한 코스의 안정적인 식별자
 * @param regionName 사용자에게 표시할 지역명
 * @param regionArea 지역의 광역 행정 구역명
 * @param imageUrl 코스 대표 이미지 주소
 * @param reason 이 코스를 추천한 이유
 * @param saved 현재 기기에서 같은 장소 구성의 코스를 저장했는지 여부
 * @param day1 첫째 날 방문 장소 목록
 * @param day2 둘째 날 방문 장소 목록
 */
@Serializable
data class CourseResponseDto(
    val courseId: String,
    val regionName: String,
    val regionArea: String? = null,
    val imageUrl: String? = null,
    val reason: String? = null,
    val saved: Boolean,
    val day1: List<SpotSummaryDto>,
    val day2: List<SpotSummaryDto>,
)

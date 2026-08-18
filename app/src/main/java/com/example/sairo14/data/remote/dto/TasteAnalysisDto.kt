package com.example.sairo14.data.remote.dto

import kotlinx.serialization.Serializable

/** 선택 사진으로 취향 분석과 추천 코스 생성을 요청하는 본문이다.
 *
 * 사진 ID의 개수와 중복 여부는 Repository가 서버 호출 전에 검증한다.
 * @param photoIds 사용자가 선택한 사진 순서의 고유 ID 목록
 */
@Serializable
data class TasteAnalysisRequestDto(
    val photoIds: List<String>,
)

/** SAIRO 취향 분석 API가 반환하는 무드 태그, 요약, 추천 코스 목록이다.
 *
 * 코스 목록이 비어도 분석이 완료된 정상 응답이므로 빈 목록을 허용한다.
 * @param moodTags 선택 사진에서 분석한 분위기 태그
 * @param summary 분석 결과를 설명하는 요약 문구
 * @param courses 추천한 코스 목록
 */
@Serializable
data class TasteAnalysisResponseDto(
    val moodTags: List<String>,
    val summary: String,
    val courses: List<CourseCardDto>,
)

/** 취향 분석 응답에 포함된 추천 코스 한 건이다.
 *
 * 코스와 장소를 식별하는 ID 및 지역명은 화면과 상세 이동에 필요하므로 서버 응답에서 누락되면
 * 역직렬화가 실패하도록 기본값을 두지 않는다.
 * @param courseId 추천 코스의 안정적인 식별자
 * @param regionName 사용자에게 표시할 지역명
 * @param regionArea 지역의 광역 행정 구역명
 * @param imageUrl 코스 대표 이미지 주소
 * @param reason 이 코스를 추천한 이유
 * @param saved 서버가 알려준 현재 저장 상태
 * @param day1 첫째 날 방문 장소 목록
 * @param day2 둘째 날 방문 장소 목록
 */
@Serializable
data class CourseCardDto(
    val courseId: String,
    val regionName: String,
    val regionArea: String? = null,
    val imageUrl: String? = null,
    val reason: String? = null,
    val saved: Boolean,
    val day1: List<SpotSummaryDto>,
    val day2: List<SpotSummaryDto>,
)

/** 추천 코스의 일차별 장소 요약 정보다.
 *
 * 위치와 운영 정보는 서버 데이터가 없을 수 있어 nullable로 보존하며, 장소명과 ID는 목록·지도 선택에
 * 필요하므로 기본값을 두지 않는다.
 * @param spotId 장소의 안정적인 식별자
 * @param name 사용자에게 표시할 장소명
 * @param lat WGS84 북위 좌표
 * @param lng WGS84 동경 좌표
 * @param imageUrl 장소 카드에 표시할 이미지 주소
 * @param operatingHours 운영 시간 문구
 * @param closedDays 휴무일 문구
 * @param parking 주차 정보 문구
 * @param contact 연락처 문구
 */
@Serializable
data class SpotSummaryDto(
    val spotId: String,
    val name: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val imageUrl: String? = null,
    val operatingHours: String? = null,
    val closedDays: String? = null,
    val parking: String? = null,
    val contact: String? = null,
)

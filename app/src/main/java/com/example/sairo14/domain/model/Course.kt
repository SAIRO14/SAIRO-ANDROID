package com.example.sairo14.domain.model

/** 여행 상세 화면과 공유용 코스를 구성하는 지역별 일자 데이터를 표현한다.
 *
 * 장소의 방문 순서는 [days]와 각 [CourseDay.places]의 순서가 소유한다.
 * @param courseId 코스를 식별하는 안정적인 ID
 * @param regionName 사용자에게 표시할 지역명
 * @param days 여행 일차 순서대로 정렬된 장소 목록
 */
data class Course(
    val courseId: String,
    val regionName: String,
    val days: List<CourseDay>,
)

/** 코스의 한 일차와 방문 순서가 있는 장소 목록을 표현한다.
 *
 * @param dayNumber 사용자에게 표시할 1부터 시작하는 일차 번호
 * @param places 해당 일차의 방문 순서대로 정렬된 장소
 */
data class CourseDay(
    val dayNumber: Int,
    val places: List<CoursePlace>,
)

/** 코스 안에서 표시할 장소의 정보와 지도 좌표를 표현한다.
 *
 * @param placeId 장소를 식별하는 안정적인 ID
 * @param name 사용자에게 표시할 장소명
 * @param imageUrl 장소 카드에 표시할 이미지 주소. 이미지가 없으면 `null`
 * @param tags 운영 시간·휴무·주차처럼 장소에 표시할 보조 정보
 * @param coordinate 지도 핀과 카메라 중심 계산에 사용할 좌표
 */
data class CoursePlace(
    val placeId: String,
    val name: String,
    val imageUrl: String?,
    val tags: List<String>,
    val coordinate: MapCoordinate,
)

/** 지도에서 장소를 표시하고 카메라 중심을 정할 때 사용하는 WGS84 좌표를 표현한다.
 *
 * @param latitude 적도 기준 북위 좌표
 * @param longitude 본초 자오선 기준 동경 좌표
 */
data class MapCoordinate(
    val latitude: Double,
    val longitude: Double,
)

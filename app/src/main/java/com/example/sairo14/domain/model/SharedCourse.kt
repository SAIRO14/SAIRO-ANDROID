package com.example.sairo14.domain.model

/** 공개 공유 링크로 조회한 읽기 전용 코스 스냅샷을 표현한다.
 *
 * 공유 링크의 식별자는 [shareId]가 소유하며, 현재 기기의 저장 여부처럼 소유자에 따라 달라지는 정보는
 * 포함하지 않는다.
 * @param shareId 공유 URL에서 전달받은 스냅샷 식별자
 * @param courseId 공유 원본 코스를 식별하는 서버 ID
 * @param regionName 사용자에게 표시할 지역명
 * @param days 여행 일차 순서대로 정렬된 장소 목록
 */
data class SharedCourse(
    val shareId: String,
    val courseId: String,
    val regionName: String,
    val days: List<CourseDay>,
)

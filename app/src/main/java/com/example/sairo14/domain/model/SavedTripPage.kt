package com.example.sairo14.domain.model

/** 저장 여행지 목록의 항목과 다음 페이지 조회 커서를 함께 표현한다.
 *
 * [nextCursor]는 서버가 제공한 값을 그대로 보관하며, `null`이면 더 조회할 페이지가 없다.
 * @param items 서버의 최신 저장순을 유지한 저장 여행지 목록
 * @param nextCursor 다음 페이지 요청에 사용할 커서
 */
data class SavedTripPage(
    val items: List<SavedTrip>,
    val nextCursor: String?,
)

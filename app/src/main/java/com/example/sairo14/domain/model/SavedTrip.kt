package com.example.sairo14.domain.model

/** 저장 목록에서 코스 상세로 이동하고 저장을 해제하는 데 필요한 여행지 요약 정보다. */
data class SavedTrip(
    val savedTripId: String,
    val courseId: String,
    val regionName: String,
    val regionArea: String?,
    val imageUrl: String?,
    val reason: String?,
    val createdAt: String,
)

package com.example.sairo14.domain.model

/** 저장 목록 폴더 카드에 필요한 여행지 정보를 표현한다. */
data class SavedTrip(
    val savedTripId: String,
    val courseId: String,
    val regionName: String,
    val description: String,
    val imageUrls: List<String>,
    val placeNames: List<String>,
)

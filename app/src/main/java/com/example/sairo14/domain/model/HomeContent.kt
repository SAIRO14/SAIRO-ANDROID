package com.example.sairo14.domain.model

/** 홈 화면에 표시할 탐색 이미지와 저장 여행지 목록을 표현한다. */
data class HomeContent(
    val discoveryImages: DiscoveryImages,
    val savedTrips: List<SavedTripSummary>,
)

/** 홈 중앙 탐색 CTA에 표시할 앞·뒤 이미지 주소를 표현한다. */
data class DiscoveryImages(
    val backImageUrl: String?,
    val frontImageUrl: String?,
)

/** 홈 캔버스 카드에 필요한 저장 여행지의 최소 정보를 표현한다. */
data class SavedTripSummary(
    val savedTripId: String,
    val courseId: String,
    val regionName: String,
    val thumbnailImageUrl: String?,
)

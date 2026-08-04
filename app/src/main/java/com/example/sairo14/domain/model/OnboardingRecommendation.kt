package com.example.sairo14.domain.model

/** 온보딩에서 선택한 사진 분위기와 어울리는 지역 추천 한 건을 표현한다. */
data class OnboardingRecommendation(
    val id: String,
    val regionName: String,
    val description: String,
    val imageUrls: List<String>,
    val placeNames: List<String>,
    val isSaved: Boolean = false,
)

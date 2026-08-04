package com.example.sairo14.domain.repository

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingRecommendation

/** 선택한 사진을 바탕으로 온보딩 지역 추천을 조회하는 도메인 계약이다. */
interface OnboardingRecommendationRepository {

    /** 선택 사진 ID와 어울리는 지역 추천을 한 번 조회한다. */
    suspend fun getRecommendations(
        selectedPhotoIds: List<String>,
    ): AppResult<List<OnboardingRecommendation>>
}

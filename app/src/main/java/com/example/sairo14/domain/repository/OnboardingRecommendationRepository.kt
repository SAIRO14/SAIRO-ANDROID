package com.example.sairo14.domain.repository

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingAnalysisResult

/** 선택한 사진을 분석해 온보딩 추천과 코스 상세 스냅샷을 제공하는 도메인 계약이다. */
interface OnboardingRecommendationRepository {

    /** 선택 사진 ID로 취향을 분석하고 무드 태그·추천·코스 상세를 한 번 생성한다. */
    suspend fun analyzeTaste(
        selectedPhotoIds: List<String>,
    ): AppResult<OnboardingAnalysisResult>
}

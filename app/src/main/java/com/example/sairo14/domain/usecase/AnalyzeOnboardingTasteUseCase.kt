package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.repository.OnboardingRecommendationRepository
import javax.inject.Inject

/** 선택 사진의 취향을 분석해 온보딩 추천 결과를 생성한다. */
class AnalyzeOnboardingTasteUseCase @Inject constructor(
    private val onboardingRecommendationRepository: OnboardingRecommendationRepository,
) {
    /** 선택 사진 ID로 무드 태그·추천 카드·코스 상세를 함께 생성한다. */
    suspend operator fun invoke(
        selectedPhotoIds: List<String>,
    ): AppResult<OnboardingAnalysisResult> =
        onboardingRecommendationRepository.analyzeTaste(selectedPhotoIds)
}

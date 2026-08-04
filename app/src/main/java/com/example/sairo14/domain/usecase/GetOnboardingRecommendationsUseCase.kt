package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingRecommendation
import com.example.sairo14.domain.repository.OnboardingRecommendationRepository
import javax.inject.Inject

/** 선택 사진의 분위기와 어울리는 온보딩 지역 추천을 조회한다. */
class GetOnboardingRecommendationsUseCase @Inject constructor(
    private val onboardingRecommendationRepository: OnboardingRecommendationRepository,
) {
    suspend operator fun invoke(
        selectedPhotoIds: List<String>,
    ): AppResult<List<OnboardingRecommendation>> =
        onboardingRecommendationRepository.getRecommendations(selectedPhotoIds)
}

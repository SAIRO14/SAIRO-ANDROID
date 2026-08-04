package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingRecommendation
import com.example.sairo14.domain.repository.OnboardingRepository
import javax.inject.Inject

/** 추천 결과 수에 따라 온보딩 완료 상태를 저장하거나 해제한다. */
class UpdateOnboardingCompletionUseCase @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
) {
    /**
     * 하나 이상의 추천이 있으면 완료 상태를 저장하고, 없으면 완료 상태를 해제한다.
     *
     * 추천의 조회와 화면 상태 표시는 호출자가 담당하며, 이 UseCase는 결과 수에 따른 완료 정책만 적용한다.
     * @param recommendations 온보딩에서 조회한 지역 추천 목록
     */
    suspend operator fun invoke(
        recommendations: List<OnboardingRecommendation>,
    ): AppResult<Unit> = if (recommendations.isEmpty()) {
        onboardingRepository.markOnboardingIncomplete()
    } else {
        onboardingRepository.markOnboardingCompleted()
    }
}

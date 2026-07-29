package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.repository.OnboardingRepository
import javax.inject.Inject

/** 앱 시작 시 저장된 온보딩 완료 여부를 조회한다. */
class GetHasCompletedOnboardingUseCase @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
) {
    suspend operator fun invoke(): AppResult<Boolean> =
        onboardingRepository.getHasCompletedOnboarding()
}

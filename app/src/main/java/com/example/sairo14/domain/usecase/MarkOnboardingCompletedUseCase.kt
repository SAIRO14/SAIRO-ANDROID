package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.repository.OnboardingRepository
import javax.inject.Inject

/** 온보딩의 추천 결과에 도달한 사용자를 완료 상태로 저장한다. */
class MarkOnboardingCompletedUseCase @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = onboardingRepository.markOnboardingCompleted()
}

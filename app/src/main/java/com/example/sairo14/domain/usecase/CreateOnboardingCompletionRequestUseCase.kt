package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingCompletionToken
import com.example.sairo14.domain.repository.OnboardingRepository
import javax.inject.Inject

/** 최신 온보딩 결과의 완료 상태를 저장할 순서 토큰을 발급한다. */
class CreateOnboardingCompletionRequestUseCase @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
) {
    /** DataStore가 관리하는 다음 완료 상태 요청 토큰을 반환한다. */
    suspend operator fun invoke(): AppResult<OnboardingCompletionToken> =
        onboardingRepository.createCompletionRequest()
}

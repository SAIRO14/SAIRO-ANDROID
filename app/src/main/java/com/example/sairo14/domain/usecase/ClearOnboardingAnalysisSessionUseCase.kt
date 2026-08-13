package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.repository.OnboardingAnalysisSessionStore
import javax.inject.Inject

/** 온보딩 탐색을 종료할 때 해당 세션에 보관한 분석 결과를 삭제한다. */
class ClearOnboardingAnalysisSessionUseCase @Inject constructor(
    private val sessionStore: OnboardingAnalysisSessionStore,
) {
    /** 지정한 온보딩 탐색 세션을 삭제한다.
     *
     * 이미 삭제됐거나 분석 결과가 아직 없는 세션도 안전하게 처리한다.
     * @param searchSessionId 삭제할 온보딩 탐색 세션 ID
     */
    suspend operator fun invoke(searchSessionId: String) {
        sessionStore.remove(searchSessionId)
    }
}

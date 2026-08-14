package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.repository.OnboardingAnalysisSessionStore
import com.example.sairo14.domain.repository.OnboardingRecommendationRepository
import javax.inject.Inject

/** 선택 사진을 분석하고 성공 결과를 온보딩 탐색 세션에 보관한다. */
class AnalyzeAndStoreOnboardingTasteUseCase @Inject constructor(
    private val onboardingRecommendationRepository: OnboardingRecommendationRepository,
    private val sessionStore: OnboardingAnalysisSessionStore,
) {
    /** 사진 분석을 요청하고 성공한 결과만 지정한 탐색 세션에 저장한다.
     *
     * 분석 실패는 저장하지 않고 그대로 반환한다. 세션 저장소가 발급한 토큰이 최신 요청일 때만
     * 결과를 저장하므로, 늦게 도착한 이전 성공 응답은 세션 결과를 덮어쓰지 못한다.
     * @param searchSessionId 분석 결과를 보관할 온보딩 탐색 세션 ID
     * @param selectedPhotoIds 사용자가 선택한 사진 순서의 ID 목록
     */
    suspend operator fun invoke(
        searchSessionId: String,
        selectedPhotoIds: List<String>,
    ): AppResult<OnboardingAnalysisResult> {
        val requestToken = sessionStore.beginRequest(searchSessionId)
        val result = onboardingRecommendationRepository.analyzeTaste(selectedPhotoIds)
        if (result is AppResult.Success) {
            sessionStore.saveIfCurrent(searchSessionId, requestToken, result.value)
        }
        return result
    }
}

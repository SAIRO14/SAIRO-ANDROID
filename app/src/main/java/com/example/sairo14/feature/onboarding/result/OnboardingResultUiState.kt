package com.example.sairo14.feature.onboarding.result

import androidx.compose.runtime.Immutable
import com.example.sairo14.domain.model.OnboardingRecommendation

/** 온보딩 추천 결과 화면의 조회·콘텐츠·오류 상태를 나타낸다. */
sealed interface OnboardingResultUiState {

    /** 온보딩 완료 상태와 추천 결과를 준비하는 중이다. */
    data object Loading : OnboardingResultUiState

    /** 추천 개수에 맞는 일반 목록 또는 부족 결과 안내를 표시할 수 있는 상태다. */
    @Immutable
    data class Content(
        val recommendations: List<OnboardingRecommendation>,
    ) : OnboardingResultUiState

    /** 완료 상태 저장 또는 추천 결과 조회에 실패해 재시도가 필요한 상태다. */
    data object Error : OnboardingResultUiState
}

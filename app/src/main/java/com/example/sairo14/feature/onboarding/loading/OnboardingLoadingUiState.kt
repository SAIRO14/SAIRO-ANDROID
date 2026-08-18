package com.example.sairo14.feature.onboarding.loading

import androidx.compose.runtime.Immutable
import com.example.sairo14.domain.model.AppError

/** 온보딩 사진 분석 화면이 표시할 준비, 콘텐츠, 오류 상태를 나타낸다. */
sealed interface OnboardingLoadingUiState {

    /** 선택한 사진 정보를 복원하는 중인 상태다. */
    data object Loading : OnboardingLoadingUiState

    /** 카드 스태킹과 서버 분석을 함께 진행하거나, 분석 태그를 표시할 수 있는 상태다. */
    @Immutable
    data class Content(
        val photos: List<OnboardingLoadingPhotoUiModel>,
        val moodTags: List<String>? = null,
    ) : OnboardingLoadingUiState

    /** 선택한 사진을 복원하지 못해 다시 선택해야 하는 상태다. */
    data object Error : OnboardingLoadingUiState

    /** 취향 분석 요청에 실패해 오류 종류에 맞는 재시도가 필요한 상태다. */
    data class AnalysisError(
        val error: AppError,
    ) : OnboardingLoadingUiState
}

/** 온보딩 로딩 카드에 필요한 사진 정보다. */
@Immutable
data class OnboardingLoadingPhotoUiModel(
    val id: String,
    val imageUrl: String,
    val contentDescription: String?,
)

const val OnboardingLoadingCardCount = 5

package com.example.sairo14.feature.onboarding

import androidx.compose.runtime.Immutable

/** 온보딩 사진 분석 화면이 표시할 준비, 콘텐츠, 오류 상태를 나타낸다. */
sealed interface OnboardingLoadingUiState {

    /** 선택한 사진 정보를 복원하는 중인 상태다. */
    data object Loading : OnboardingLoadingUiState

    /** 카드 스태킹에 사용할 사진을 모두 준비한 상태다. */
    @Immutable
    data class Content(
        val photos: List<OnboardingLoadingPhotoUiModel>,
    ) : OnboardingLoadingUiState

    /** 선택한 사진을 복원하지 못해 다시 선택해야 하는 상태다. */
    data object Error : OnboardingLoadingUiState
}

/** 온보딩 로딩 카드에 필요한 사진 정보다. */
@Immutable
data class OnboardingLoadingPhotoUiModel(
    val id: String,
    val imageUrl: String,
    val contentDescription: String?,
)

const val OnboardingLoadingCardCount = 5

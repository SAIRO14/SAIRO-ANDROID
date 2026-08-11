package com.example.sairo14.feature.onboarding.select

import androidx.compose.runtime.Immutable

/** 온보딩 사진 선택 화면이 렌더링할 로딩, 빈 목록, 오류, 콘텐츠 상태를 나타낸다. */
sealed interface OnboardingPhotoSelectUiState {

    /** 사진 후보를 불러오는 중인 상태다. */
    data object Loading : OnboardingPhotoSelectUiState

    /** 선택할 사진 후보가 없는 상태다. */
    data object Empty : OnboardingPhotoSelectUiState

    /** 사진 후보를 불러오지 못해 재시도가 필요한 상태다. */
    data object Error : OnboardingPhotoSelectUiState

    /** 사진 후보와 사용자의 선택·확인 상태를 표시할 수 있는 상태다. */
    @Immutable
    data class Content(
        val photos: List<OnboardingPhotoUiModel>,
        val selectedPhotoIds: Set<String> = emptySet(),
        val viewedPhotoIds: Set<String> = emptySet(),
        val minimumSelectionCount: Int = MinimumSelectionCount,
    ) : OnboardingPhotoSelectUiState {
        val selectedPhotos: List<OnboardingPhotoUiModel>
            get() = photos.filter { photo -> photo.id in selectedPhotoIds }

        val canComplete: Boolean
            get() = selectedPhotoIds.size >= minimumSelectionCount
    }
}

/** 사진 선택 화면에서 카드와 썸네일을 그리는 데 필요한 UI 전용 정보다. */
@Immutable
data class OnboardingPhotoUiModel(
    val id: String,
    val imageUrl: String,
    val contentDescription: String?,
)

const val MinimumSelectionCount = 3

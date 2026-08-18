package com.example.sairo14.feature.onboarding.select

import androidx.compose.runtime.Immutable
import com.example.sairo14.domain.model.AppError

/** 온보딩 사진 선택 화면이 렌더링할 로딩, 빈 목록, 오류, 콘텐츠 상태를 나타낸다. */
sealed interface OnboardingPhotoSelectUiState {

    /** 사진 후보를 불러오는 중인 상태다. */
    data object Loading : OnboardingPhotoSelectUiState

    /** 선택할 사진 후보가 없는 상태다. */
    data object Empty : OnboardingPhotoSelectUiState

    /** 사진 후보를 불러오지 못해 오류 종류에 맞는 안내가 필요한 상태다. */
    data class Error(
        val error: AppError,
    ) : OnboardingPhotoSelectUiState

    /** 사진 후보와 선택 순서를 표시할 수 있는 상태다.
     *
     * [selectedPhotoIds]는 사용자가 선택한 순서를 보존한다. 새 사진 선택은
     * [maximumSelectionCount]에 도달하면 막고, 이미 선택한 사진의 해제는 항상 허용한다.
     * @param photos 화면에 표시할 사진 후보
     * @param selectedPhotoIds 선택한 사진 ID와 선택 순서
     * @param minimumSelectionCount 완료에 필요한 최소 사진 수
     * @param maximumSelectionCount 선택할 수 있는 최대 사진 수
     */
    @Immutable
    data class Content(
        val photos: List<OnboardingPhotoUiModel>,
        val selectedPhotoIds: List<String> = emptyList(),
        val minimumSelectionCount: Int = MinimumSelectionCount,
        val maximumSelectionCount: Int = MaximumSelectionCount,
    ) : OnboardingPhotoSelectUiState {
        val selectedPhotos: List<OnboardingPhotoUiModel>
            get() {
                val photosById = photos.associateBy(OnboardingPhotoUiModel::id)
                return selectedPhotoIds.mapNotNull(photosById::get)
            }

        val canComplete: Boolean
            get() = selectedPhotoIds.size in minimumSelectionCount..maximumSelectionCount

        val hasReachedMaximumSelection: Boolean
            get() = selectedPhotoIds.size >= maximumSelectionCount
    }
}

/** 사진 선택 화면에서 카드와 썸네일을 그리는 데 필요한 UI 전용 정보다. */
@Immutable
data class OnboardingPhotoUiModel(
    val id: String,
    val imageUrl: String,
    val contentDescription: String?,
)

const val MinimumSelectionCount = 5
const val MaximumSelectionCount = 10

package com.example.sairo14.feature.onboarding.loading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.PhotoCandidate
import com.example.sairo14.domain.usecase.GetPhotoCandidatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 온보딩 로딩 화면에서 선택한 사진을 카드 모션용 UI 상태로 복원한다.
 *
 * Route가 전달한 사진 ID의 순서를 유지해 [OnboardingLoadingUiState]로 노출한다. 애니메이션 진행과
 * 결과 화면 이동은 화면이 소유하고, 이 ViewModel은 사진 복원 실패만 오류 상태로 전달한다.
 */
@HiltViewModel
class OnboardingLoadingViewModel @Inject constructor(
    private val getPhotoCandidates: GetPhotoCandidatesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<OnboardingLoadingUiState>(OnboardingLoadingUiState.Loading)

    val uiState: StateFlow<OnboardingLoadingUiState> = _uiState.asStateFlow()

    private var selectedPhotoIds: List<String>? = null

    /**
     * 전달된 사진 ID로 카드에 표시할 후보 정보를 한 번만 복원한다.
     * @param selectedPhotoIds 사진 선택 화면에서 확정한 사진 ID와 표시 순서
     * @param force 동일한 ID여도 후보 정보를 다시 조회할지 여부
     */
    fun load(selectedPhotoIds: List<String>, force: Boolean = false) {
        if (!force && this.selectedPhotoIds == selectedPhotoIds) return

        this.selectedPhotoIds = selectedPhotoIds
        viewModelScope.launch {
            _uiState.value = OnboardingLoadingUiState.Loading
            _uiState.value = when (val result = getPhotoCandidates()) {
                is AppResult.Success -> result.value.toLoadingContent(selectedPhotoIds)
                    ?: OnboardingLoadingUiState.Error

                is AppResult.Failure -> OnboardingLoadingUiState.Error
            }
        }
    }

    /** 사진 복원 오류에서 동일한 선택 ID로 다시 시도한다. */
    fun retry() {
        selectedPhotoIds?.let { photoIds -> load(photoIds, force = true) }
    }
}

private fun List<PhotoCandidate>.toLoadingContent(
    selectedPhotoIds: List<String>,
): OnboardingLoadingUiState.Content? {
    val uniqueIds = selectedPhotoIds.distinct()
    if (uniqueIds.size < OnboardingLoadingCardCount) return null

    val photosById = associateBy(PhotoCandidate::id)
    val selectedPhotos = uniqueIds.map { photoId ->
        photosById[photoId]?.toLoadingUiModel() ?: return null
    }

    return OnboardingLoadingUiState.Content(photos = selectedPhotos)
}

private fun PhotoCandidate.toLoadingUiModel(): OnboardingLoadingPhotoUiModel =
    OnboardingLoadingPhotoUiModel(
        id = id,
        imageUrl = imageUrl,
        contentDescription = contentDescription,
    )

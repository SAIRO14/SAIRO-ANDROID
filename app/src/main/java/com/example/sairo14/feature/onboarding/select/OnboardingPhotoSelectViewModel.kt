package com.example.sairo14.feature.onboarding.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.PhotoCandidate
import com.example.sairo14.domain.usecase.GetPhotoCandidatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 온보딩 사진 선택 화면의 후보 목록과 선택 상태를 관리한다.
 *
 * 로딩·빈 목록·오류·콘텐츠는 [OnboardingPhotoSelectUiState]로 노출하며, 카드와 썸네일 이벤트는
 * 선택 집합을 변경한다. 완료 가능한 선택은 [OnboardingPhotoSelectEffect.SelectionCompleted]로
 * 전달하고 화면 이동은 Route가 담당한다.
 */
@HiltViewModel
class OnboardingPhotoSelectViewModel @Inject constructor(
    private val getPhotoCandidates: GetPhotoCandidatesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<OnboardingPhotoSelectUiState>(
        OnboardingPhotoSelectUiState.Loading,
    )

    val uiState: StateFlow<OnboardingPhotoSelectUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<OnboardingPhotoSelectEffect>(extraBufferCapacity = 1)

    val effect: SharedFlow<OnboardingPhotoSelectEffect> = _effect.asSharedFlow()

    init {
        loadPhotoCandidates()
    }

    /** 오류 상태에서 사진 후보 목록을 다시 조회한다. */
    fun retry() {
        if (_uiState.value is OnboardingPhotoSelectUiState.Error) {
            loadPhotoCandidates()
        }
    }

    /** 사진 카드를 눌렀을 때 해당 사진의 선택 상태를 전환한다. */
    fun togglePhotoSelection(photoId: String) {
        updateContent { content ->
            if (content.photos.none { photo -> photo.id == photoId }) return@updateContent content

            val selectedPhotoIds = content.selectedPhotoIds.toMutableSet().apply {
                if (!add(photoId)) remove(photoId)
            }

            content.copy(
                selectedPhotoIds = selectedPhotoIds,
                viewedPhotoIds = content.viewedPhotoIds + photoId,
            )
        }
    }

    /** 선택 썸네일의 제거 버튼을 눌렀을 때 해당 사진을 선택 목록에서 제거한다. */
    fun removePhotoSelection(photoId: String) {
        updateContent { content ->
            content.copy(selectedPhotoIds = content.selectedPhotoIds - photoId)
        }
    }

    /** Pager에서 사진이 보였을 때 확인한 사진으로 기록한다. */
    fun markPhotoViewed(photoId: String) {
        updateContent { content ->
            if (content.photos.any { photo -> photo.id == photoId }) {
                content.copy(viewedPhotoIds = content.viewedPhotoIds + photoId)
            } else {
                content
            }
        }
    }

    /** 최소 선택 수를 만족할 때 선택한 사진 ID를 화면 이동 효과로 전달한다. */
    fun completeSelection() {
        val content = _uiState.value as? OnboardingPhotoSelectUiState.Content ?: return
        if (!content.canComplete) return

        _effect.tryEmit(
            OnboardingPhotoSelectEffect.SelectionCompleted(
                photoIds = content.selectedPhotos.map { photo -> photo.id },
            ),
        )
    }

    private fun loadPhotoCandidates() {
        viewModelScope.launch {
            _uiState.value = OnboardingPhotoSelectUiState.Loading
            _uiState.value = when (val result = getPhotoCandidates()) {
                is AppResult.Success -> result.value
                    .map(PhotoCandidate::toUiModel)
                    .takeIf(List<*>::isNotEmpty)
                    ?.let(OnboardingPhotoSelectUiState::Content)
                    ?: OnboardingPhotoSelectUiState.Empty

                is AppResult.Failure -> OnboardingPhotoSelectUiState.Error
            }
        }
    }

    private inline fun updateContent(
        transform: (OnboardingPhotoSelectUiState.Content) -> OnboardingPhotoSelectUiState.Content,
    ) {
        val content = _uiState.value as? OnboardingPhotoSelectUiState.Content ?: return
        _uiState.value = transform(content)
    }
}

/** 사진 후보 Domain 모델을 온보딩 화면 전용 모델로 변환한다. */
private fun PhotoCandidate.toUiModel(): OnboardingPhotoUiModel =
    OnboardingPhotoUiModel(
        id = id,
        imageUrl = imageUrl,
        contentDescription = contentDescription,
    )

/** 사진 선택 화면이 Route에 전달하는 한 번만 처리할 사용자 흐름 결과다. */
sealed interface OnboardingPhotoSelectEffect {

    /** 최소 선택 수를 만족해 다음 화면으로 전달할 사진 ID 목록이다. */
    data class SelectionCompleted(
        val photoIds: List<String>,
    ) : OnboardingPhotoSelectEffect
}

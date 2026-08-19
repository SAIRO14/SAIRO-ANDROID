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
 * 온보딩 사진 선택 화면의 후보 목록과 선택·확인 상태를 관리한다.
 *
 * 로딩·빈 목록·오류·콘텐츠는 [OnboardingPhotoSelectUiState]로 노출하며, 카드와 썸네일 이벤트는
 * 선택 순서를 변경하고 Pager에 표시된 사진은 확인 상태로 기록한다. 완료 가능한 선택은
 * [OnboardingPhotoSelectEffect.SelectionCompleted]로 전달하고 화면 이동은 Route가 담당한다.
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

    /** 사진 카드를 눌렀을 때 선택 상태를 전환하고 선택 순서를 갱신한다.
     *
     * 최대 선택 수에 도달한 경우에는 이미 선택한 사진의 해제만 허용한다.
     */
    fun togglePhotoSelection(photoId: String) {
        updateContent { content ->
            if (content.photos.none { photo -> photo.id == photoId }) return@updateContent content

            val selectedIndex = content.selectedPhotoIds.indexOf(photoId)
            val selectedPhotoIds = when {
                selectedIndex >= 0 -> content.selectedPhotoIds.filterNot { id -> id == photoId }
                content.hasReachedMaximumSelection -> content.selectedPhotoIds
                else -> content.selectedPhotoIds + photoId
            }

            content.copy(selectedPhotoIds = selectedPhotoIds)
        }
    }

    /** 선택 썸네일의 제거 버튼을 눌렀을 때 해당 사진을 선택 목록에서 제거한다. */
    fun removePhotoSelection(photoId: String) {
        updateContent { content ->
            content.copy(selectedPhotoIds = content.selectedPhotoIds.filterNot { id -> id == photoId })
        }
    }

    /** Pager에 표시된 사진을 확인 상태로 기록하고 같은 사진은 한 번만 유지한다. */
    fun markPhotoViewed(photoId: String) {
        updateContent { content ->
            if (content.photos.none { photo -> photo.id == photoId } || photoId in content.viewedPhotoIds) {
                content
            } else {
                content.copy(viewedPhotoIds = content.viewedPhotoIds + photoId)
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
                animationPhotos = content.selectedPhotos.take(OnboardingLoadingAnimationPhotoCount),
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

                is AppResult.Failure -> OnboardingPhotoSelectUiState.Error(result.error)
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

    /** 최소 선택 수를 만족해 다음 화면으로 전달할 ID와 애니메이션 사진이다. */
    data class SelectionCompleted(
        val photoIds: List<String>,
        val animationPhotos: List<OnboardingPhotoUiModel>,
    ) : OnboardingPhotoSelectEffect
}

private const val OnboardingLoadingAnimationPhotoCount = 5

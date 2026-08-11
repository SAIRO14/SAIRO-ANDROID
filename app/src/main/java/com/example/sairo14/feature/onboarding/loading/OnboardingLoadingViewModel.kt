package com.example.sairo14.feature.onboarding.loading

import androidx.lifecycle.ViewModel
import com.example.sairo14.core.navigation.OnboardingAnimationPhoto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 온보딩 로딩 화면에서 전달받은 선택 사진을 카드 모션용 UI 상태로 변환한다.
 *
 * 사진 URL은 Route가 소유하며 이 ViewModel은 API를 다시 호출하지 않는다. 앞 5장의 고유한 사진이
 * 전달되면 즉시 [OnboardingLoadingUiState.Content]를 노출하고, 잘못된 전달값만 오류로 처리한다.
 */
@HiltViewModel
class OnboardingLoadingViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<OnboardingLoadingUiState>(OnboardingLoadingUiState.Loading)

    val uiState: StateFlow<OnboardingLoadingUiState> = _uiState.asStateFlow()

    private var animationPhotos: List<OnboardingAnimationPhoto>? = null

    /** 전달된 사진으로 로딩 애니메이션 카드 상태를 준비한다.
     *
     * @param animationPhotos 사진 선택 순서의 앞 5장으로 구성한 애니메이션 카드 정보
     */
    fun load(animationPhotos: List<OnboardingAnimationPhoto>) {
        if (this.animationPhotos == animationPhotos) return

        this.animationPhotos = animationPhotos
        _uiState.value = animationPhotos.toLoadingContent()
            ?: OnboardingLoadingUiState.Error
    }
}

private fun List<OnboardingAnimationPhoto>.toLoadingContent(): OnboardingLoadingUiState.Content? {
    val uniquePhotos = distinctBy(OnboardingAnimationPhoto::id)
    if (uniquePhotos.size != OnboardingLoadingCardCount) return null

    return OnboardingLoadingUiState.Content(
        photos = uniquePhotos.map(OnboardingAnimationPhoto::toLoadingUiModel),
    )
}

private fun OnboardingAnimationPhoto.toLoadingUiModel(): OnboardingLoadingPhotoUiModel =
    OnboardingLoadingPhotoUiModel(
        id = id,
        imageUrl = imageUrl,
        contentDescription = null,
    )

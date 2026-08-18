package com.example.sairo14.feature.onboarding.intro

import androidx.lifecycle.ViewModel
import com.example.sairo14.core.dummyimage.SeasonalDummyImageProvider
import com.example.sairo14.core.navigation.SairoNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 온보딩 인트로 화면의 상태와 사용자 이벤트를 관리한다.
 *
 * 계절에 맞춰 선택한 세 쌍의 로컬 이미지는 [OnboardingIntroUiState]로 노출한다. 화면 이동은
 * [SairoNavigator]가 담당하므로 이 ViewModel은 내비게이션 명령을 소유하지 않는다.
 */
@HiltViewModel
class OnboardingIntroViewModel @Inject constructor(
    seasonalDummyImageProvider: SeasonalDummyImageProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        OnboardingIntroUiState(
            imagePairs = seasonalDummyImageProvider.imagesForToday().onboardingPairs,
        ),
    )

    val uiState: StateFlow<OnboardingIntroUiState> = _uiState.asStateFlow()
}

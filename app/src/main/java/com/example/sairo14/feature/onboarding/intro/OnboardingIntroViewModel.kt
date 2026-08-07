package com.example.sairo14.feature.onboarding.intro

import androidx.lifecycle.ViewModel
import com.example.sairo14.core.navigation.SairoNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 온보딩 인트로 화면의 상태와 사용자 이벤트를 관리한다.
 *
 * 서버에서 가져올 앞·뒤 두 장의 이미지 URL은 [OnboardingIntroUiState]로 노출한다. 화면 이동은
 * [SairoNavigator]가 담당하므로 이 ViewModel은 내비게이션 명령을 소유하지 않는다.
 */
@HiltViewModel
class OnboardingIntroViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingIntroUiState())

    val uiState: StateFlow<OnboardingIntroUiState> = _uiState.asStateFlow()
}

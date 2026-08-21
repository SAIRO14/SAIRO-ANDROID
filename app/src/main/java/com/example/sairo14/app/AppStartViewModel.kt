package com.example.sairo14.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.usecase.GetHasCompletedOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 앱 시작 목적지의 로딩·성공·오류 상태를 관리한다.
 *
 * DataStore에서 완료 여부를 한 번 읽어 [AppStartUiState]로 노출한다. 백스택 변경은
 * [SairoNavigator]가 담당한다. 처음 시작할 때는 브랜드 스플래시가 짧게 보이도록 로딩 상태를
 * 최소 시간 동안 유지하며, 읽기 실패 시에는 이전 상태를 추측하지 않고 재시도 상태를 표시한다.
 */
@HiltViewModel
class AppStartViewModel @Inject constructor(
    private val getHasCompletedOnboarding: GetHasCompletedOnboardingUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AppStartUiState>(AppStartUiState.Loading)

    val uiState: StateFlow<AppStartUiState> = _uiState.asStateFlow()

    init {
        loadStartDestination(minimumLoadingDurationMillis = MINIMUM_SPLASH_DURATION_MILLIS)
    }

    /** DataStore를 다시 읽어 앱 시작 목적지를 재판단한다. */
    fun retry() {
        loadStartDestination()
    }

    private fun loadStartDestination(
        minimumLoadingDurationMillis: Long = 0,
    ) {
        viewModelScope.launch {
            _uiState.value = AppStartUiState.Loading
            val loadingStart = TimeSource.Monotonic.markNow()

            val nextUiState = when (val result = getHasCompletedOnboarding()) {
                is AppResult.Success -> AppStartUiState.Ready(
                    destination = if (result.value) {
                        AppStartDestination.Home
                    } else {
                        AppStartDestination.OnboardingIntro
                    },
                )

                is AppResult.Failure -> AppStartUiState.Error(result.error)
            }

            val remainingDurationMillis =
                (minimumLoadingDurationMillis - loadingStart.elapsedNow().inWholeMilliseconds)
                    .coerceAtLeast(0)
            delay(remainingDurationMillis)
            _uiState.value = nextUiState
        }
    }

    private companion object {
        const val MINIMUM_SPLASH_DURATION_MILLIS = 500L
    }
}

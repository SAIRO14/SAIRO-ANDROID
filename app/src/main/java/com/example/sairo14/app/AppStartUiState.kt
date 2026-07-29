package com.example.sairo14.app

import com.example.sairo14.domain.model.AppError

/** 앱 시작 단계에서 렌더링할 상태를 나타낸다. */
sealed interface AppStartUiState {
    /** DataStore에서 시작 목적지를 읽는 중인 상태다. */
    data object Loading : AppStartUiState

    /** 초기 백스택을 구성할 수 있는 상태다. */
    data class Ready(
        val destination: AppStartDestination,
    ) : AppStartUiState

    /** 저장소에 접근하지 못해 사용자의 재시도가 필요한 상태다. */
    data class Error(
        val error: AppError,
    ) : AppStartUiState
}

/** 온보딩 완료 여부로 결정되는 앱의 최초 목적지다. */
enum class AppStartDestination {
    Home,
    OnboardingIntro,
}

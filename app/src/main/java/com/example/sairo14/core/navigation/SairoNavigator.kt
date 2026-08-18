package com.example.sairo14.core.navigation

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Sairo의 Nav3 백스택을 변경하는 앱 수준 내비게이션 명령을 제공한다.
 *
 * 백스택의 생성과 수명은 호출자가 소유하며, 이 객체는 화면·ViewModel 대신 이동 정책만 적용한다.
 * @param backStack 현재 앱에서 표시할 Nav3 목적지 백스택
 * @param onOnboardingSessionEnded 백스택에서 더 이상 참조되지 않는 온보딩 세션 ID를 전달받는 콜백
 */
@Stable
class SairoNavigator(
    private val backStack: NavBackStack<NavKey>,
    private val onOnboardingSessionEnded: (String) -> Unit = {},
) {
    /** 주어진 목적지를 현재 백스택의 최상단에 추가한다. */
    fun navigate(route: SairoRoute) {
        backStack.add(route)
    }

    /** 현재 최상단 목적지와 다를 때만 새 목적지를 추가한다. */
    fun navigateSingleTop(route: SairoRoute) {
        if (backStack.lastOrNull() != route) {
            backStack.add(route)
        }
    }

    /**
     * 현재 목적지를 교체해 완료 화면에서 이전 중간 단계를 다시 표시하지 않는다.
     * @param route 현재 최상단을 대체할 목적지
     */
    fun replaceTop(route: SairoRoute) {
        if (backStack.isNotEmpty()) {
            val replacedRoute = backStack[backStack.lastIndex]
            backStack[backStack.lastIndex] = route
            notifyOnboardingSessionEndedIfNeeded(replacedRoute)
        } else {
            backStack.add(route)
        }
    }

    /**
     * 현재 목적지를 닫고 이전 목적지로 이동한다.
     *
     * 루트 목적지는 유지하므로, 앱 종료 여부는 Activity의 시스템 뒤로가기 정책이 결정한다.
     */
    fun navigateUp() {
        if (backStack.size > 1) {
            removeTop()
        }
    }

    /**
     * 현재 온보딩 인트로부터 새 여행지 탐색 세션을 시작한다.
     *
     * 인트로 뒤에 남아 있는 사진 선택·분석·결과 목적지를 닫고 새 세션 Route를 추가한다. 일반
     * 뒤로가기는 이 메서드를 호출하지 않으므로, 기존 사진 선택 상태는 백스택에 남아 있는 동안 유지된다.
     * @param searchSessionId 새 탐색과 이전 탐색의 NavEntry 상태를 구분하는 고유 식별자
     */
    fun startNewOnboardingSearch(searchSessionId: String) {
        val introIndex = backStack.indexOfLast { route -> route is OnboardingIntroRoute }

        if (introIndex < 0) return

        while (backStack.lastIndex > introIndex) {
            removeTop()
        }
        backStack.add(OnboardingPhotoSelectRoute(searchSessionId))
    }

    /**
     * 백스택에서 가장 가까운 홈 목적지까지 현재 화면들을 닫는다.
     *
     * 홈 목적지가 없는 백스택은 변경하지 않는다.
     */
    fun popToHome() {
        val homeIndex = backStack.indexOfLast { route -> route == HomeRoute }

        if (homeIndex < 0) return

        while (backStack.lastIndex > homeIndex) {
            removeTop()
        }
    }

    private fun removeTop() {
        val removedRoute = backStack.removeAt(backStack.lastIndex)
        notifyOnboardingSessionEndedIfNeeded(removedRoute)
    }

    private fun notifyOnboardingSessionEndedIfNeeded(removedRoute: NavKey) {
        val endedSessionId = removedRoute.onboardingSessionIdOrNull() ?: return

        if (backStack.none { route -> route.onboardingSessionIdOrNull() == endedSessionId }) {
            onOnboardingSessionEnded(endedSessionId)
        }
    }
}

private fun NavKey.onboardingSessionIdOrNull(): String? = when (this) {
    is OnboardingPhotoSelectRoute -> searchSessionId
    is OnboardingLoadingRoute -> searchSessionId
    is OnboardingResultRoute -> searchSessionId
    is TravelDetailRoute -> onboardingSessionId
    else -> null
}

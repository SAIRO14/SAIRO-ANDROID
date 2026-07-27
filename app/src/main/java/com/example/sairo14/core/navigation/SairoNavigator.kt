package com.example.sairo14.core.navigation

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Sairo의 Nav3 백스택을 변경하는 앱 수준 내비게이션 명령을 제공한다.
 *
 * 백스택의 생성과 수명은 호출자가 소유하며, 이 객체는 화면·ViewModel 대신 이동 정책만 적용한다.
 * @param backStack 현재 앱에서 표시할 Nav3 목적지 백스택
 */
@Stable
class SairoNavigator(
    private val backStack: NavBackStack<NavKey>,
) {
    /** 주어진 목적지를 현재 백스택의 최상단에 추가한다. */
    fun navigate(route: SairoRoute) {
        backStack.add(route)
    }

    /**
     * 현재 목적지를 닫고 이전 목적지로 이동한다.
     *
     * 루트 목적지는 유지하므로, 앱 종료 여부는 Activity의 시스템 뒤로가기 정책이 결정한다.
     */
    fun navigateUp() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
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
            backStack.removeAt(backStack.lastIndex)
        }
    }
}

package com.example.sairo14.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.sairo14.feature.home.HomeScreen
import com.example.sairo14.feature.onboarding.OnboardingScreen

/**
 * Nav3 목적지 키를 Sairo 화면으로 변환해 현재 백스택을 표시한다.
 *
 * 목적지별 화면 등록만 담당하며, 백스택 변경은 [SairoNavigator]이 수행한다.
 * @param backStack 표시할 앱 내비게이션 백스택
 * @param navigator 화면에서 사용할 앱 수준 내비게이션 명령
 */
@Composable
fun SairoNavDisplay(
    backStack: NavBackStack<NavKey>,
    navigator: SairoNavigator,
) {
    NavDisplay(
        backStack = backStack,
        onBack = navigator::navigateUp,
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreen()
            }
            entry<OnboardingIntroRoute> {
                OnboardingScreen()
            }
        },
    )
}

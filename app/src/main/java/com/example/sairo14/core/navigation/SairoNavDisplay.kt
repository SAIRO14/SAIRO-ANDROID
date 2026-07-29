package com.example.sairo14.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.sairo14.feature.main.MainScreen

private const val ForwardEnterDurationMillis = 300
private const val ForwardExitDurationMillis = 225

private val ForwardEnterEasing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
private val ForwardExitEasing = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)

/**
 * Sairo 목적지와 공통 화면 전환 정책을 연결해 현재 백스택을 표시한다.
 *
 * 전방 이동은 Figma 모션 원칙에 따라 300ms의 ease-out 전환을 사용하고,
 * 앱의 뒤로가기 액션은 즉시 복귀한다. 시스템 predictive back은 플랫폼 기본 동작을 유지한다.
 * @param navigationViewModel 앱 수준 백스택과 내비게이션 명령을 소유하는 ViewModel
 */
@Composable
fun SairoNavDisplay(
    navigationViewModel: AppNavigationViewModel,
) {
    NavDisplay(
        backStack = navigationViewModel.backStack,
        onBack = navigationViewModel::navigateUp,
        transitionSpec = {
            (
                slideInHorizontally(
                    animationSpec = tween(
                        durationMillis = ForwardEnterDurationMillis,
                        easing = ForwardEnterEasing,
                    ),
                ) { fullWidth -> fullWidth } + fadeIn(
                    animationSpec = tween(
                        durationMillis = ForwardEnterDurationMillis,
                        easing = ForwardEnterEasing,
                    ),
                )
            ).togetherWith(
                slideOutHorizontally(
                    animationSpec = tween(
                        durationMillis = ForwardExitDurationMillis,
                        easing = ForwardExitEasing,
                    ),
                ) { fullWidth -> -fullWidth / 4 } + fadeOut(
                    animationSpec = tween(
                        durationMillis = ForwardExitDurationMillis,
                        easing = ForwardExitEasing,
                    ),
                ),
            )
        },
        popTransitionSpec = {
            EnterTransition.None togetherWith ExitTransition.None
        },
        entryProvider = entryProvider {
            entry<MainRoute> {
                MainScreen()
            }
        },
    )
}

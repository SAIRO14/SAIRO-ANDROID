package com.example.sairo14.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.ui.NavDisplay
import com.example.sairo14.feature.home.HomeRoute as HomeScreenRoute
import com.example.sairo14.feature.onboarding.OnboardingIntroRoute as OnboardingIntroScreenRoute
import com.example.sairo14.feature.onboarding.OnboardingLoadingRoute as OnboardingLoadingScreenRoute
import com.example.sairo14.feature.onboarding.OnboardingPhotoSelectRoute as OnboardingPhotoSelectScreenRoute
import com.example.sairo14.feature.onboarding.OnboardingResultRoute as OnboardingResultScreenRoute
import com.example.sairo14.feature.savedtrip.SavedTripsRoute as SavedTripsScreenRoute

private const val ForwardEnterDurationMillis = 300
private const val ForwardExitDurationMillis = 225
private const val LoadingResultDissolveDurationMillis = 250

private val ForwardEnterEasing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
private val ForwardExitEasing = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)

/**
 * Sairo 목적지와 공통 화면 전환 정책을 연결해 현재 백스택을 표시한다.
 *
 * 전방 이동에는 Figma 모션 원칙에 따른 가로 슬라이드·페이드 전환을 적용하고, 뒤로가기는 즉시
 * 이전 화면을 표시한다. 백스택 변경은 [SairoNavigator]가 담당하며 이 Composable은 이를 렌더링만 한다.
 * @param backStack 현재 앱에서 표시할 Nav3 목적지 백스택
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
            entry<HomeRoute> {
                HomeScreenRoute(
                    onFindTripClick = {
                        navigator.navigateSingleTop(
                            OnboardingIntroRoute(
                                entryPoint = OnboardingIntroEntryPoint.Home,
                            ),
                        )
                    },
                    onFolderClick = {
                        navigator.navigateSingleTop(SavedTripsRoute)
                    },
                )
            }
            entry<SavedTripsRoute> {
                SavedTripsScreenRoute(
                    onBackClick = navigator::navigateUp,
                    onHomeClick = navigator::popToHome,
                    onFindTripClick = {
                        navigator.navigateSingleTop(
                            OnboardingIntroRoute(
                                entryPoint = OnboardingIntroEntryPoint.Home,
                            ),
                        )
                    },
                )
            }
            entry<OnboardingIntroRoute> { route ->
                OnboardingIntroScreenRoute(
                    entryPoint = route.entryPoint,
                    onBackClick = navigator::navigateUp,
                    onHomeClick = navigator::popToHome,
                    onStartClick = {
                        navigator.navigate(OnboardingPhotoSelectRoute)
                    },
                )
            }
            entry<OnboardingPhotoSelectRoute> {
                OnboardingPhotoSelectScreenRoute(
                    onSelectionComplete = { photoIds ->
                        navigator.navigateSingleTop(OnboardingLoadingRoute(photoIds))
                    },
                )
            }
            entry<OnboardingLoadingRoute> { route ->
                OnboardingLoadingScreenRoute(
                    selectedPhotoIds = route.selectedPhotoIds,
                    onFinished = {
                        navigator.replaceTop(OnboardingResultRoute(route.selectedPhotoIds))
                    },
                    onBackClick = navigator::navigateUp,
                )
            }
            entry<OnboardingResultRoute>(
                metadata = metadata {
                    put(NavDisplay.TransitionKey) {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = LoadingResultDissolveDurationMillis,
                                easing = LinearEasing,
                            ),
                        ).togetherWith(
                            fadeOut(
                                animationSpec = tween(
                                    durationMillis = LoadingResultDissolveDurationMillis,
                                    easing = LinearEasing,
                                ),
                            ),
                        )
                    }
                },
            ) { route ->
                OnboardingResultScreenRoute(
                    selectedPhotoIds = route.selectedPhotoIds,
                    onBackClick = navigator::navigateUp,
                    onHomeClick = navigator::popToHome,
                    onRequestAgainClick = navigator::navigateUp,
                )
            }
        },
    )
}

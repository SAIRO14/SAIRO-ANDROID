package com.example.sairo14.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Test

class SairoNavigatorTest {

    @Test
    fun `startNewOnboardingSearch replaces the current search flow after intro`() {
        val introRoute = OnboardingIntroRoute(OnboardingIntroEntryPoint.Home)
        val oldSelectionRoute = OnboardingPhotoSelectRoute(searchSessionId = "old-session")
        val backStack = NavBackStack<NavKey>(
            HomeRoute,
            introRoute,
            oldSelectionRoute,
            OnboardingResultRoute(
                searchSessionId = "old-session",
                selectedPhotoIds = listOf("photo-1"),
            ),
        )
        val navigator = SairoNavigator(backStack)

        navigator.startNewOnboardingSearch(searchSessionId = "new-session")

        assertEquals(
            listOf<NavKey>(
                HomeRoute,
                introRoute,
                OnboardingPhotoSelectRoute(searchSessionId = "new-session"),
            ),
            backStack.toList(),
        )
    }

    @Test
    fun `재추천은 이전 세션을 정리하고 새 세션 Route를 만든다`() {
        val endedSessionIds = mutableListOf<String>()
        val backStack = NavBackStack<NavKey>(
            HomeRoute,
            OnboardingIntroRoute(),
            OnboardingPhotoSelectRoute(searchSessionId = "old-session"),
            OnboardingResultRoute(
                searchSessionId = "old-session",
                selectedPhotoIds = listOf("photo-1"),
            ),
        )
        val navigator = SairoNavigator(
            backStack = backStack,
            onOnboardingSessionEnded = endedSessionIds::add,
        )

        navigator.startNewOnboardingSearch(searchSessionId = "new-session")

        assertEquals(listOf("old-session"), endedSessionIds)
        assertEquals(OnboardingPhotoSelectRoute("new-session"), backStack.last())
    }

    @Test
    fun `navigateUp retains the existing photo selection route`() {
        val selectionRoute = OnboardingPhotoSelectRoute(searchSessionId = "session")
        val backStack = NavBackStack<NavKey>(
            HomeRoute,
            OnboardingIntroRoute(),
            selectionRoute,
            OnboardingResultRoute(
                searchSessionId = "session",
                selectedPhotoIds = listOf("photo-1"),
            ),
        )
        val navigator = SairoNavigator(backStack)

        navigator.navigateUp()

        assertEquals(
            listOf<NavKey>(
                HomeRoute,
                OnboardingIntroRoute(),
                selectionRoute,
            ),
            backStack.toList(),
        )
    }

    @Test
    fun `popToHome closes child routes and clears the completed onboarding session`() {
        val endedSessionIds = mutableListOf<String>()
        val backStack = NavBackStack<NavKey>(
            HomeRoute,
            OnboardingIntroRoute(),
            OnboardingPhotoSelectRoute(searchSessionId = "session"),
            TravelDetailRoute(courseId = "course-1", onboardingSessionId = "session"),
        )
        val navigator = SairoNavigator(
            backStack = backStack,
            onOnboardingSessionEnded = endedSessionIds::add,
        )

        navigator.popToHome()

        assertEquals(listOf<NavKey>(HomeRoute), backStack.toList())
        assertEquals(listOf("session"), endedSessionIds)
    }

    @Test
    fun `popToHome keeps the home route when it is already at the top`() {
        val backStack = NavBackStack<NavKey>(HomeRoute)
        val navigator = SairoNavigator(backStack)

        navigator.popToHome()

        assertEquals(listOf<NavKey>(HomeRoute), backStack.toList())
    }

    @Test
    fun `마지막 온보딩 Route가 제거되면 세션 정리를 요청한다`() {
        val endedSessionIds = mutableListOf<String>()
        val backStack = NavBackStack<NavKey>(
            HomeRoute,
            OnboardingIntroRoute(),
            OnboardingPhotoSelectRoute(searchSessionId = "session"),
        )
        val navigator = SairoNavigator(
            backStack = backStack,
            onOnboardingSessionEnded = endedSessionIds::add,
        )

        navigator.navigateUp()

        assertEquals(listOf("session"), endedSessionIds)
    }

    @Test
    fun `상세 화면을 닫아도 결과 Route가 남아 있으면 세션을 유지한다`() {
        val endedSessionIds = mutableListOf<String>()
        val backStack = NavBackStack<NavKey>(
            HomeRoute,
            OnboardingPhotoSelectRoute(searchSessionId = "session"),
            OnboardingResultRoute(
                searchSessionId = "session",
                selectedPhotoIds = listOf("photo-1"),
            ),
            TravelDetailRoute(courseId = "course-1", onboardingSessionId = "session"),
        )
        val navigator = SairoNavigator(
            backStack = backStack,
            onOnboardingSessionEnded = endedSessionIds::add,
        )

        navigator.navigateUp()

        assertEquals(emptyList<String>(), endedSessionIds)
    }

    @Test
    fun `상세 Route는 이전 화면의 북마크 표시와 저장 항목 ID를 보존한다`() {
        val route = TravelDetailRoute(
            courseId = "course-1",
            onboardingSessionId = "session",
            initialSaved = true,
            savedTripId = "saved-trip-1",
        )

        assertEquals(true, route.initialSaved)
        assertEquals("saved-trip-1", route.savedTripId)
    }

    @Test
    fun `공유 코스 Route는 공유 스냅샷 식별자만 보존한다`() {
        val route = SharedCourseRoute(shareId = "7429b36b9d")

        assertEquals("7429b36b9d", route.shareId)
    }

    @Test
    fun `공유 코스 화면을 닫으면 이전 홈 화면으로 돌아간다`() {
        val backStack = NavBackStack<NavKey>(HomeRoute, SharedCourseRoute("share-1"))
        val navigator = SairoNavigator(backStack)

        navigator.navigateUp()

        assertEquals(listOf<NavKey>(HomeRoute), backStack.toList())
    }
}

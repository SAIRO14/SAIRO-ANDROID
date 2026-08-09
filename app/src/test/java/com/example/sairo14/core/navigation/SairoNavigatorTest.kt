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
}

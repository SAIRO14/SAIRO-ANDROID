package com.example.sairo14.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.sairo14.core.navigation.HomeRoute
import com.example.sairo14.core.navigation.OnboardingIntroRoute
import com.example.sairo14.core.navigation.SairoNavDisplay
import com.example.sairo14.core.navigation.SairoNavigator

/** Sairo의 최상위 Compose UI와 Nav3 백스택을 조립한다. */
@Composable
fun SairoApp() {
    val backStack = rememberNavBackStack(
        HomeRoute,
        OnboardingIntroRoute,
    )
    val navigator = remember(backStack) { SairoNavigator(backStack) }

    SairoNavDisplay(
        backStack = backStack,
        navigator = navigator,
    )
}

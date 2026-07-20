package com.example.sairo14.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.sairo14.feature.main.MainScreen

@Composable
fun SairoNavDisplay(
    navigationViewModel: AppNavigationViewModel,
) {
    NavDisplay(
        backStack = navigationViewModel.backStack,
        onBack = navigationViewModel::navigateUp,
        entryProvider = entryProvider {
            entry<MainRoute> {
                MainScreen()
            }
        },
    )
}

package com.example.sairo14.app

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.sairo14.core.navigation.AppNavigationViewModel
import com.example.sairo14.core.navigation.SairoNavDisplay

@Composable
fun SairoApp(
    navigationViewModel: AppNavigationViewModel = hiltViewModel(),
) {
    SairoNavDisplay(navigationViewModel)
}

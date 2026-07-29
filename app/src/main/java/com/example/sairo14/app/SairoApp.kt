package com.example.sairo14.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.component.SairoButton
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.navigation.HomeRoute
import com.example.sairo14.core.navigation.OnboardingIntroRoute
import com.example.sairo14.core.navigation.SairoNavDisplay
import com.example.sairo14.core.navigation.SairoNavigator

/** Sairo의 시작 상태를 확인한 뒤 최초 Nav3 백스택을 조립한다. */
@Composable
fun SairoApp(
    viewModel: AppStartViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    when (uiState) {
        AppStartUiState.Loading -> AppStartLoadingScreen()
        is AppStartUiState.Error -> AppStartErrorScreen(onRetryClick = viewModel::retry)
        is AppStartUiState.Ready -> SairoNavigation(
            startDestination = uiState.destination,
        )
    }
}

@Composable
private fun SairoNavigation(
    startDestination: AppStartDestination,
) {
    val backStack = when (startDestination) {
        AppStartDestination.Home -> rememberNavBackStack(HomeRoute)
        AppStartDestination.OnboardingIntro -> rememberNavBackStack(
            HomeRoute,
            OnboardingIntroRoute,
        )
    }
    val navigator = remember(backStack) { SairoNavigator(backStack) }

    SairoNavDisplay(
        backStack = backStack,
        navigator = navigator,
    )
}

@Composable
private fun AppStartLoadingScreen() {
    val colors = SairoTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundCanvas),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator(color = colors.accentBase)
            Text(
                text = stringResource(R.string.app_start_loading),
                color = colors.textMuted,
                style = SairoTextStyles.bodyLight16,
            )
        }
    }
}

@Composable
private fun AppStartErrorScreen(
    onRetryClick: () -> Unit,
) {
    val colors = SairoTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundCanvas)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.app_start_error),
                color = colors.textPrimary,
                style = SairoTextStyles.bodyLight18,
            )
            SairoButton(
                text = stringResource(R.string.app_start_retry),
                onClick = onRetryClick,
            )
        }
    }
}

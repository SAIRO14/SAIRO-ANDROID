package com.example.sairo14.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

object SairoTheme {
    val colors: SairoColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSairoColors.current
}

@Composable
fun ProvideSairoColors(
    colors: SairoColors,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSairoColors provides colors,
        content = content,
    )
}

private fun sairoLightMaterialColorScheme(colors: SairoColors) = lightColorScheme(
    primary = colors.actionDefault,
    onPrimary = colors.textWhite,
    primaryContainer = colors.accentTint,
    onPrimaryContainer = colors.textAccentAa,
    secondary = colors.highlightBase,
    onSecondary = colors.highlightInk,
    secondaryContainer = colors.highlightTint,
    onSecondaryContainer = colors.highlightInk,
    error = colors.warningBase,
    onError = colors.textWhite,
    errorContainer = colors.warningBackground,
    onErrorContainer = colors.warningText,
    background = colors.backgroundCanvas,
    onBackground = colors.textPrimary,
    surface = colors.surfaceDefault,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.surfaceSunken,
    onSurfaceVariant = colors.textMuted,
    surfaceContainerHigh = colors.surfaceRaised,
    outline = colors.borderDefault,
    outlineVariant = colors.borderSubtle,
    scrim = colors.overlayScrim,
)

/** Provides Sairo's light color scheme and typography to the app UI tree. */
@Composable
fun SairoTheme(
    content: @Composable () -> Unit,
) {
    ProvideSairoColors(colors = defaultSairoColors) {
        MaterialTheme(
            colorScheme = sairoLightMaterialColorScheme(defaultSairoColors),
            typography = SairoTypography,
            content = content,
        )
    }
}

package com.example.sairo14.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SairoLightMaterialColorScheme = lightColorScheme(
    primary = SairoSemanticColors.actionDefault,
    onPrimary = SairoSemanticColors.textWhite,
    primaryContainer = SairoSemanticColors.accentTint,
    onPrimaryContainer = SairoSemanticColors.textAccentAa,
    secondary = SairoSemanticColors.highlightBase,
    onSecondary = SairoSemanticColors.highlightInk,
    secondaryContainer = SairoSemanticColors.highlightTint,
    onSecondaryContainer = SairoSemanticColors.highlightInk,
    error = SairoSemanticColors.warningBase,
    onError = SairoSemanticColors.textWhite,
    errorContainer = SairoSemanticColors.warningBackground,
    onErrorContainer = SairoSemanticColors.warningText,
    background = SairoSemanticColors.backgroundCanvas,
    onBackground = SairoSemanticColors.textPrimary,
    surface = SairoSemanticColors.surfaceDefault,
    onSurface = SairoSemanticColors.textPrimary,
    surfaceVariant = SairoSemanticColors.surfaceSunken,
    onSurfaceVariant = SairoSemanticColors.textMuted,
    outline = SairoSemanticColors.borderDefault,
    outlineVariant = SairoSemanticColors.borderSubtle,
    scrim = SairoSemanticColors.overlayScrim,
)

/** Provides Sairo's light color scheme and typography to the app UI tree. */
@Composable
fun SairoTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SairoLightMaterialColorScheme,
        typography = SairoTypography,
        content = content,
    )
}

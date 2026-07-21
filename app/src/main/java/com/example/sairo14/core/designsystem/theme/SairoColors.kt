package com.example.sairo14.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.sairo14.core.designsystem.token.SairoColor

/** Sairo-specific semantic colors not represented by Material 3's [ColorScheme]. */
@Immutable
data class SairoColors(
    // Background and surface
    val backgroundCanvas: Color,
    val surfaceDefault: Color,
    val surfaceRaised: Color,
    val surfaceSunken: Color,
    val surfaceHeader: Color,

    // Text
    val textPrimary: Color,
    val textWhite: Color,
    val textMuted: Color,
    val textSubtle: Color,
    val textAccent: Color,
    val textAccentAa: Color,

    // Border
    val borderSubtle: Color,
    val borderDefault: Color,
    val borderStrong: Color,
    val borderInverse: Color,

    // Primary action
    val actionDefault: Color,
    val actionPressed: Color,
    val actionDisabled: Color,
    val actionText: Color,
    val actionTextDisabled: Color,

    // Outline action
    val actionOutlineBackground: Color,
    val actionOutlineBackgroundPressed: Color,
    val actionOutlineBorder: Color,
    val actionOutlineText: Color,

    // Brand and highlight
    val accentBase: Color,
    val accentStrong: Color,
    val accentTint: Color,
    val highlightBase: Color,
    val highlightTint: Color,
    val highlightInk: Color,

    // Chip and tag
    val chipGreenBackground: Color,
    val chipGreenText: Color,
    val chipLimeBackground: Color,
    val chipLimeText: Color,
    val tagBackground: Color,
    val tagText: Color,

    // Selection and focus
    val selectionRing: Color,
    val selectionBadge: Color,
    val selectionBadgeIcon: Color,

    // Warning
    val warningBase: Color,
    val warningBackground: Color,
    val warningBorder: Color,
    val warningText: Color,

    // Overlay and indicators
    val overlayScrim: Color,
    val focusRing: Color,
    val indicatorEmpty: Color,
    val indicatorFilled: Color,
    val indicatorActive: Color,
)

/** The Sairo light color set derived from the Figma design system. */
val defaultSairoColors = SairoColors(
    // Background and surface
    backgroundCanvas = SairoColor.Gray50,
    surfaceDefault = SairoColor.Gray50,
    surfaceRaised = SairoColor.White,
    surfaceSunken = SairoColor.Gray300,
    surfaceHeader = SairoColor.Gray100.copy(alpha = 0.01f),

    // Text
    textPrimary = SairoColor.Gray900,
    textWhite = SairoColor.White,
    textMuted = SairoColor.Gray700,
    textSubtle = SairoColor.Gray600,
    textAccent = SairoColor.Green600,
    textAccentAa = SairoColor.Green700,

    // Border
    borderSubtle = SairoColor.Gray300,
    borderDefault = SairoColor.Gray400,
    borderStrong = SairoColor.Gray500,
    borderInverse = SairoColor.White,

    // Primary action
    actionDefault = SairoColor.Gray900,
    actionPressed = SairoColor.Gray800,
    actionDisabled = SairoColor.Gray400,
    actionText = SairoColor.Green500,
    actionTextDisabled = SairoColor.Gray600,

    // Outline action
    actionOutlineBackground = SairoColor.White,
    actionOutlineBackgroundPressed = SairoColor.Gray50,
    actionOutlineBorder = SairoColor.Gray500,
    actionOutlineText = SairoColor.Gray800,

    // Brand and highlight
    accentBase = SairoColor.Green500,
    accentStrong = SairoColor.Green600,
    accentTint = SairoColor.Green50,
    highlightBase = SairoColor.Lime300,
    highlightTint = SairoColor.Lime100,
    highlightInk = SairoColor.Lime900,

    // Chip and tag
    chipGreenBackground = SairoColor.Green50,
    chipGreenText = SairoColor.Green900,
    chipLimeBackground = SairoColor.Lime300,
    chipLimeText = SairoColor.Lime900,
    tagBackground = SairoColor.Green50,
    tagText = SairoColor.Green900,

    // Selection and focus
    selectionRing = SairoColor.Green600,
    selectionBadge = SairoColor.Green500,
    selectionBadgeIcon = SairoColor.Green900,

    // Warning
    warningBase = SairoColor.Warning500,
    warningBackground = SairoColor.Warning50,
    warningBorder = SairoColor.Warning500,
    warningText = SairoColor.Warning800,

    // Overlay and indicators
    overlayScrim = SairoColor.Gray900.copy(alpha = 0.60f),
    focusRing = SairoColor.Green600,
    indicatorEmpty = SairoColor.Gray400,
    indicatorFilled = SairoColor.Gray600,
    indicatorActive = SairoColor.Green500,
)

val LocalSairoColors = staticCompositionLocalOf { defaultSairoColors }

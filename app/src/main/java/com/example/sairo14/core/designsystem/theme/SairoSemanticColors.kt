package com.example.sairo14.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import com.example.sairo14.core.designsystem.token.SairoColor

/**
 * Semantic color roles for the current light Sairo theme.
 *
 * Components should use these roles rather than palette values so a dark scheme can
 * supply the same roles later without changing component APIs.
 */
object SairoSemanticColors {
    val backgroundCanvas = SairoColor.Gray50

    val surfaceDefault = SairoColor.Gray50
    val surfaceRaised = SairoColor.White
    val surfaceSunken = SairoColor.Gray300
    val surfaceHeader = SairoColor.Gray100.copy(alpha = 0.01f)

    val textPrimary = SairoColor.Gray900
    val textWhite = SairoColor.White
    val textMuted = SairoColor.Gray700
    val textSubtle = SairoColor.Gray600
    val textAccent = SairoColor.Green600
    val textAccentAa = SairoColor.Green700

    val borderSubtle = SairoColor.Gray300
    val borderDefault = SairoColor.Gray400
    val borderStrong = SairoColor.Gray500
    val borderInverse = SairoColor.White

    val actionDefault = SairoColor.Gray900
    val actionPressed = SairoColor.Gray800
    val actionDisabled = SairoColor.Gray400
    val actionText = SairoColor.Green500
    val actionTextDisabled = SairoColor.Gray600

    val actionOutlineBackground = SairoColor.White
    val actionOutlineBackgroundPressed = SairoColor.Gray50
    val actionOutlineBorder = SairoColor.Gray500
    val actionOutlineText = SairoColor.Gray800

    val accentBase = SairoColor.Green500
    val accentStrong = SairoColor.Green600
    val accentTint = SairoColor.Green50

    val highlightBase = SairoColor.Lime300
    val highlightTint = SairoColor.Lime100
    val highlightInk = SairoColor.Lime900

    val chipGreenBackground = SairoColor.Green50
    val chipGreenText = SairoColor.Green900
    val chipLimeBackground = SairoColor.Lime300
    val chipLimeText = SairoColor.Lime900

    val selectionRing = SairoColor.Green600
    val selectionBadge = SairoColor.Green500
    val selectionBadgeIcon = SairoColor.Green900

    val tagBackground = SairoColor.Green50
    val tagText = SairoColor.Green900

    val warningBase = SairoColor.Warning500
    val warningBackground = SairoColor.Warning50
    val warningBorder = SairoColor.Warning500
    val warningText = SairoColor.Warning800

    val overlayScrim = SairoColor.Gray900.copy(alpha = 0.60f)
    val focusRing = SairoColor.Green600

    val indicatorEmpty = SairoColor.Gray400
    val indicatorFilled = SairoColor.Gray600
    val indicatorActive = SairoColor.Green500
}

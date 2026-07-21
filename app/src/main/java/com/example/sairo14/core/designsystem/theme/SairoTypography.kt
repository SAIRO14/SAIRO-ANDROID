package com.example.sairo14.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.sairo14.R

/** Freesentation family supplied with the project. */
private val Freesentation = FontFamily(
    Font(R.font.freesentation_light, FontWeight.Light),
    Font(R.font.freesentation_regular, FontWeight.Normal),
)

private fun sairoTextStyle(
    fontWeight: FontWeight,
    fontSize: Int,
    lineHeight: Int,
    letterSpacing: Float = 0f,
) = TextStyle(
    fontFamily = Freesentation,
    fontWeight = fontWeight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.em,
    // The Figma line-height should be the layout height, without Android font padding.
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

/**
 * Named styles from the Figma Text Styles page.
 *
 * Use these when a screen needs to refer to a specific Sairo type token. Prefer
 * [SairoTypography] when Material 3's semantic typography slots are sufficient.
 */
object SairoTextStyles {
    val displayLight28 = sairoTextStyle(FontWeight.Light, fontSize = 28, lineHeight = 36)
    val displayLight24 = sairoTextStyle(FontWeight.Light, fontSize = 24, lineHeight = 31)

    val headRegular22 = sairoTextStyle(FontWeight.Normal, fontSize = 22, lineHeight = 28)
    val headRegular20 = sairoTextStyle(FontWeight.Normal, fontSize = 20, lineHeight = 26)
    val headRegular18 = sairoTextStyle(FontWeight.Normal, fontSize = 18, lineHeight = 23)
    val headRegular16 = sairoTextStyle(FontWeight.Normal, fontSize = 16, lineHeight = 22)
    val headRegular14 = sairoTextStyle(FontWeight.Normal, fontSize = 14, lineHeight = 20)

    val bodyLight22 = sairoTextStyle(FontWeight.Light, fontSize = 22, lineHeight = 28, letterSpacing = -0.01f)
    val bodyLight20 = sairoTextStyle(FontWeight.Light, fontSize = 20, lineHeight = 26, letterSpacing = -0.01f)
    val bodyLight18 = sairoTextStyle(FontWeight.Light, fontSize = 18, lineHeight = 23, letterSpacing = -0.01f)
    val bodyLight16 = sairoTextStyle(FontWeight.Light, fontSize = 16, lineHeight = 22, letterSpacing = -0.01f)
    val bodyLight14 = sairoTextStyle(FontWeight.Light, fontSize = 14, lineHeight = 20, letterSpacing = -0.01f)
    val bodyLight12 = sairoTextStyle(FontWeight.Light, fontSize = 12, lineHeight = 17, letterSpacing = -0.01f)
}

/** Material 3 semantic mapping for the Sairo type scale. */
val SairoTypography = Typography(
    displayLarge = SairoTextStyles.displayLight28,
    displayMedium = SairoTextStyles.displayLight24,
    headlineLarge = SairoTextStyles.headRegular22,
    headlineMedium = SairoTextStyles.headRegular20,
    headlineSmall = SairoTextStyles.headRegular18,
    titleLarge = SairoTextStyles.headRegular16,
    titleMedium = SairoTextStyles.headRegular14,
    bodyLarge = SairoTextStyles.bodyLight22,
    bodyMedium = SairoTextStyles.bodyLight20,
    bodySmall = SairoTextStyles.bodyLight18,
    labelLarge = SairoTextStyles.bodyLight16,
    labelMedium = SairoTextStyles.bodyLight14,
    labelSmall = SairoTextStyles.bodyLight12,
)

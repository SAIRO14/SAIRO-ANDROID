package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.extension.noRippleClickable
import com.skydoves.cloudy.cloudy

/** Android 헤더에 적용할 Figma 배경과 내비게이션 구성을 정의한다. */
enum class SairoHeaderVariant {
    Home,
    Sub,
    SubFilled,
    ActionOnly,
}

/**
 * 홈 또는 하위 화면의 시스템 상태 표시줄을 포함한 상단 헤더를 표시한다.
 *
 * 제목과 액션 상태는 호출자가 소유한다. [SairoHeaderVariant.Home]은 로고를, Sub 변형은
 * 뒤로가기 버튼과 [title]을, [SairoHeaderVariant.ActionOnly]는 우측 액션만 표시한다.
 * [backdropState]를 전달하면 SubFilled를 제외한 변형의 배경에 콘텐츠 뒤쪽을 흐리는 backdrop
 * blur를 적용한다. 상태 표시줄 영역은 시스템 inset만큼 자동 확보한다.
 * @param variant Figma의 Home, Sub, SubFilled 또는 ActionOnly 헤더 변형
 * @param modifier 헤더에 적용할 Modifier
 * @param title Sub 변형에 표시할 제목
 * @param onBackClick Sub 변형의 뒤로가기 버튼을 클릭했을 때 호출할 동작
 * @param actionIcon 우측 액션에 표시할 아이콘. `null`이면 우측 액션을 표시하지 않는다
 * @param actionContentDescription 우측 액션 아이콘의 접근성 설명
 * @param onActionClick 우측 액션을 클릭했을 때 호출할 동작
 * @param backdropState 헤더 뒤 콘텐츠와 구형 Android blur 정책을 소유한 상태
 * @param iconTint 헤더 아이콘에 적용할 색상. 기본값 [Color.Unspecified]은 아이콘 원본 색상을 유지한다
 * @param enabled `false`이면 헤더 액션 클릭을 전달하지 않는지 여부
 */
@Composable
fun SairoHeader(
    variant: SairoHeaderVariant,
    modifier: Modifier = Modifier,
    title: String? = null,
    onBackClick: (() -> Unit)? = null,
    actionIcon: Painter? = null,
    actionContentDescription: String? = null,
    onActionClick: (() -> Unit)? = null,
    backdropState: SairoBackdropState? = null,
    iconTint: Color = Color.Unspecified,
    enabled: Boolean = true,
) {
    val colors = SairoTheme.colors
    val backgroundColor = when (variant) {
        SairoHeaderVariant.SubFilled -> colors.surfaceDefault
        SairoHeaderVariant.Home,
        SairoHeaderVariant.Sub,
        SairoHeaderVariant.ActionOnly,
        -> colors.surfaceHeader
    }
    val isBackdropBlurEnabled = backdropState != null && variant != SairoHeaderVariant.SubFilled

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isBackdropBlurEnabled) {
                    Modifier.cloudy(
                        sky = requireNotNull(backdropState).sky,
                        radius = HeaderBackdropBlurRadius,
                        cpuBlurEnabled = backdropState.cpuBlurEnabled,
                    )
                } else {
                    Modifier
                },
            )
            .background(backgroundColor)
            .statusBarsPadding()
            .height(HeaderNavigationHeight),
    ) {
        when (variant) {
            SairoHeaderVariant.Home -> HomeHeaderContents(
                enabled = enabled,
                actionIcon = actionIcon,
                actionContentDescription = actionContentDescription,
                onActionClick = onActionClick,
                iconTint = iconTint,
            )

            SairoHeaderVariant.Sub,
            SairoHeaderVariant.SubFilled,
            -> SubHeaderContents(
                title = title.orEmpty(),
                enabled = enabled,
                onBackClick = onBackClick,
                actionIcon = actionIcon,
                actionContentDescription = actionContentDescription,
                onActionClick = onActionClick,
                iconTint = iconTint,
            )

            SairoHeaderVariant.ActionOnly -> ActionOnlyHeaderContents(
                enabled = enabled,
                actionIcon = actionIcon,
                actionContentDescription = actionContentDescription,
                onActionClick = onActionClick,
                iconTint = iconTint,
            )
        }
    }
}

@Composable
private fun HomeHeaderContents(
    enabled: Boolean,
    actionIcon: Painter?,
    actionContentDescription: String?,
    onActionClick: (() -> Unit)?,
    iconTint: Color,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        HeaderAction(
            painter = painterResource(R.drawable.ic_logo_black),
            contentDescription = stringResource(R.string.sairo_header_logo),
            modifier = Modifier.align(Alignment.CenterStart),
            enabled = enabled,
            onClick = null,
            tint = iconTint,
        )
        actionIcon?.let { painter ->
            HeaderAction(
                painter = painter,
                contentDescription = actionContentDescription,
                modifier = Modifier.align(Alignment.CenterEnd),
                enabled = enabled,
                onClick = onActionClick,
                tint = iconTint,
            )
        }
    }
}

@Composable
private fun SubHeaderContents(
    title: String,
    enabled: Boolean,
    onBackClick: (() -> Unit)?,
    actionIcon: Painter?,
    actionContentDescription: String?,
    onActionClick: (() -> Unit)?,
    iconTint: Color,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderAction(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.sairo_header_back),
                size = BackActionTouchSize,
                enabled = enabled,
                onClick = onBackClick,
                tint = iconTint,
            )
            Text(
                text = title,
                color = SairoTheme.colors.textPrimary,
                style = SairoTextStyles.bodyLight18,
            )
        }
        actionIcon?.let { painter ->
            HeaderAction(
                painter = painter,
                contentDescription = actionContentDescription,
                modifier = Modifier.align(Alignment.CenterEnd),
                enabled = enabled,
                onClick = onActionClick,
                tint = iconTint,
            )
        }
    }
}

@Composable
private fun ActionOnlyHeaderContents(
    enabled: Boolean,
    actionIcon: Painter?,
    actionContentDescription: String?,
    onActionClick: (() -> Unit)?,
    iconTint: Color,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        actionIcon?.let { painter ->
            HeaderAction(
                painter = painter,
                contentDescription = actionContentDescription,
                modifier = Modifier.align(Alignment.CenterEnd),
                enabled = enabled,
                onClick = onActionClick,
                tint = iconTint,
            )
        }
    }
}

@Composable
private fun HeaderAction(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = HeaderActionTouchSize,
    enabled: Boolean,
    onClick: (() -> Unit)?,
    tint: Color,
) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) {
                    Modifier.noRippleClickable(
                        onClick = onClick,
                        isEnabled = enabled,
                        role = Role.Button,
                    )
                } else {
                    Modifier
                },
            )
            .padding((size - HeaderIconSize) / 2),
        tint = tint,
    )
}

private val HeaderNavigationHeight = 56.dp
private val HeaderActionTouchSize = 44.dp
private val BackActionTouchSize = 36.dp
private val HeaderIconSize = 24.dp
private const val HeaderBackdropBlurRadius = 20

@Preview(name = "Sairo Header / Home", showBackground = true, widthDp = 360)
@Composable
private fun SairoHeaderHomePreview() {
    SairoTheme {
        SairoHeader(
            variant = SairoHeaderVariant.Home,
            actionIcon = painterResource(R.drawable.ic_home),
            actionContentDescription = stringResource(R.string.sairo_header_home),
            onActionClick = {},
        )
    }
}

@Preview(name = "Sairo Header / Sub", showBackground = true, widthDp = 360)
@Composable
private fun SairoHeaderSubPreview() {
    SairoTheme {
        SairoHeader(
            variant = SairoHeaderVariant.Sub,
            title = stringResource(R.string.sairo_header_preview_title),
            onBackClick = {},
            actionIcon = painterResource(R.drawable.ic_home),
            actionContentDescription = stringResource(R.string.sairo_header_home),
            onActionClick = {},
        )
    }
}

@Preview(name = "Sairo Header / Sub Filled", showBackground = true, widthDp = 360)
@Composable
private fun SairoHeaderSubFilledPreview() {
    SairoTheme {
        SairoHeader(
            variant = SairoHeaderVariant.SubFilled,
            title = stringResource(R.string.sairo_header_preview_title),
            onBackClick = {},
            actionIcon = painterResource(R.drawable.ic_home),
            actionContentDescription = stringResource(R.string.sairo_header_home),
            onActionClick = {},
        )
    }
}

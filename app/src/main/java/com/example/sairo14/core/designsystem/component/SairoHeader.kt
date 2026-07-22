package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.skydoves.cloudy.Sky
import com.skydoves.cloudy.cloudy

/** Android 헤더에 적용할 Figma 배경과 내비게이션 구성을 정의한다. */
enum class SairoHeaderVariant {
    Home,
    Sub,
    SubFilled,
}

/**
 * 홈 또는 하위 화면의 시스템 상태 표시줄을 포함한 상단 헤더를 표시한다.
 *
 * 제목과 액션 상태는 호출자가 소유한다. [SairoHeaderVariant.Home]은 로고를, Sub 변형은
 * 뒤로가기 버튼과 [title]을 표시하며, 우측에는 필요한 아이콘 액션을 선택적으로 표시한다.
 * [backdropSky]를 전달하면 Home·Sub 변형의 배경에만 콘텐츠 뒤쪽을 흐리는 backdrop blur를
 * 적용한다. 상태 표시줄 영역은 시스템 inset만큼 자동 확보한다.
 * @param variant Figma의 Home, Sub 또는 SubFilled 헤더 변형
 * @param modifier 헤더에 적용할 Modifier
 * @param title Sub 변형에 표시할 제목
 * @param onBackClick Sub 변형의 뒤로가기 버튼을 클릭했을 때 호출할 동작
 * @param actionIcon 우측 액션에 표시할 아이콘. `null`이면 우측 액션을 표시하지 않는다
 * @param actionContentDescription 우측 액션 아이콘의 접근성 설명
 * @param onActionClick 우측 액션을 클릭했을 때 호출할 동작
 * @param backdropSky 헤더 뒤 콘텐츠를 캡처한 Cloudy [Sky]. 화면의 콘텐츠에 `sky(sky)`를 적용해 생성한다
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
    backdropSky: Sky? = null,
    enabled: Boolean = true,
) {
    val colors = SairoTheme.colors
    val backgroundColor = when (variant) {
        SairoHeaderVariant.SubFilled -> colors.surfaceDefault
        SairoHeaderVariant.Home,
        SairoHeaderVariant.Sub,
        -> colors.surfaceHeader
    }
    val isBackdropBlurEnabled = backdropSky != null && variant != SairoHeaderVariant.SubFilled

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isBackdropBlurEnabled) {
                    Modifier.cloudy(
                        sky = requireNotNull(backdropSky),
                        radius = HeaderBackdropBlurRadius,
                        cpuBlurEnabled = false,
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
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        HeaderAction(
            painter = painterResource(R.drawable.ic_logo_black),
            contentDescription = stringResource(R.string.sairo_header_logo),
            modifier = Modifier.align(Alignment.CenterStart),
            enabled = enabled,
            onClick = null,
        )
        actionIcon?.let { painter ->
            HeaderAction(
                painter = painter,
                contentDescription = actionContentDescription,
                modifier = Modifier.align(Alignment.CenterEnd),
                enabled = enabled,
                onClick = onActionClick,
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
) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        enabled = enabled,
                        interactionSource = null,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding((size - HeaderIconSize) / 2),
        tint = Color.Unspecified,
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

package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.designsystem.token.SairoShadowStyles
import com.example.sairo14.core.extension.noRippleClickable
import com.example.sairo14.core.extension.sairoDropShadow

/**
 * 선택 가능한 항목을 나타내는 공통 chip을 표시한다.
 *
 * 선택 상태는 호출자가 [selected]로 소유하며, 이 컴포넌트는 상태를 직접 변경하지 않고
 * [onClick]만 전달한다. 단일 선택 항목임을 접근성 서비스에 알리기 위해 RadioButton 역할과
 * 선택 상태를 함께 제공한다.
 *
 * @param text chip에 표시할 문구
 * @param selected 현재 선택 여부
 * @param onClick chip을 선택했을 때 호출할 동작
 * @param modifier chip에 적용할 Modifier
 */
@Composable
fun SairoChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SairoTheme.colors
    val shape = RoundedCornerShape(999.dp)
    val backgroundColor = if (selected) colors.actionDefault else colors.surfaceRaised
    val contentColor = if (selected) colors.actionText else colors.textMuted

    Row(
        modifier = modifier
            .sairoDropShadow(
                shape = shape,
                shadowStyle = SairoShadowStyles.glowSubtle,
            )
            .background(backgroundColor, shape)
            .border(width = 1.dp, color = colors.borderDefault, shape = shape)
            .noRippleClickable(
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics { this.selected = selected }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            color = contentColor,
            style = SairoTextStyles.headRegular16,
        )
    }
}

@Preview(name = "Sairo Chip", showBackground = true)
@Composable
private fun SairoChipPreview() {
    SairoTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SairoChip(
                text = androidx.compose.ui.res.stringResource(R.string.sairo_chip_preview_selected),
                selected = true,
                onClick = {},
            )
            SairoChip(
                text = androidx.compose.ui.res.stringResource(R.string.sairo_chip_preview_unselected),
                selected = false,
                onClick = {},
            )
        }
    }
}

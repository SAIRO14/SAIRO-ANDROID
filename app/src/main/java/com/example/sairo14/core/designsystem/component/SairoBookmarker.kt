package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.designsystem.token.SairoShadowStyles
import com.example.sairo14.core.extension.sairoDropShadow

/**
 * 저장 여부를 나타내고 저장 상태 변경 동작을 전달하는 북마커를 표시한다.
 *
 * 저장 상태는 호출자가 [saved]로 소유하며, 이 컴포넌트는 아이콘을 바꾸고 [onClick]만 전달한다.
 * @param saved 현재 저장 여부
 * @param onClick 북마커를 클릭했을 때 호출할 동작
 * @param modifier 북마커에 적용할 Modifier
 * @param size 북마커의 가로·세로 크기. 기본값은 24dp다.
 * @param enabled `false`이면 클릭 이벤트를 전달하지 않는지 여부
 */
@Composable
fun SairoBookmarker(
    saved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    enabled: Boolean = true,
) {
    Icon(
        painter = painterResource(
            if (saved) R.drawable.ic_bookmark_fill else R.drawable.ic_bookmark_outline,
        ),
        contentDescription = stringResource(
            if (saved) R.string.sairo_bookmark_saved else R.string.sairo_bookmark_unsaved,
        ),
        modifier = modifier
            .size(size)
            .sairoDropShadow(
                shape = RectangleShape,
                shadowStyle = SairoShadowStyles.subtle,
            )
            .clickable(
                enabled = enabled,
                interactionSource = null,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { this.selected = saved },
        tint = Color.Unspecified,
    )
}

@Preview(name = "Sairo Bookmarker", showBackground = true)
@Composable
private fun SairoBookmarkerPreview() {
    SairoTheme {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        ) {
            SairoBookmarker(saved = false, onClick = {})
            SairoBookmarker(saved = true, onClick = {})
        }
    }
}

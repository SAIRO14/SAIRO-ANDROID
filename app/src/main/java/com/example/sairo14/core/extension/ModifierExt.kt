package com.example.sairo14.core.extension

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import com.example.sairo14.core.designsystem.token.SairoShadowStyle

/**
 * 클릭 동작은 유지하면서 Material ripple 표시만 제거한다.

 * 눌림 상태에 따라 색상이나 크기를 변경해야 하는 컴포넌트는 이 Modifier 대신
 * `MutableInteractionSource`를 `remember`하고 해당 상태를 관찰해야 한다.
 *
 * @param onClick 클릭 완료 시 실행할 동작
 * @param isEnabled `false`이면 클릭 이벤트를 받지 않는지 여부
 */
fun Modifier.noRippleClickable(
    onClick: () -> Unit,
    isEnabled: Boolean = true,
): Modifier = clickable(
        indication = null,
        interactionSource = null,
        onClick = onClick,
        enabled = isEnabled,
    )

/**
 * Figma에 정의된 그림자 스타일의 모든 레이어를 수정 대상의 뒤에 적용한다.
 *
 * blur, spread, color, offset의 정확한 값은 [SairoShadowStyle]이 관리한다.
 * 컴포넌트 표면 뒤에 그림자가 그려지도록 `background` 또는 `clip`보다 앞에 적용한다.
 *
 * @param shape 그림자와 컴포넌트 표면에 함께 적용할 형태
 * @param shadowStyle 렌더링할 이름 기반의 Figma 그림자 스타일
 */
fun Modifier.sairoDropShadow(
    shape: Shape,
    shadowStyle: SairoShadowStyle,
): Modifier = shadowStyle.layers.fold(this) { modifier, layer ->
    modifier.dropShadow(
        shape = shape,
        shadow = Shadow(
            radius = layer.blurRadius,
            spread = layer.spread,
            color = layer.color,
            offset = layer.offset,
        ),
    )
}

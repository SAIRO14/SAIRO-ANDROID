package com.example.sairo14.core.map

/** 카카오 지도 SDK에 전달할 px 단위 뷰포트 여백이다. */
internal data class SairoMapViewportPaddingPx(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
)

/**
 * 지도 viewport를 넘지 않도록 여백을 보정한다.
 *
 * 왼쪽·위쪽 여백을 먼저 유지하고, 남은 영역 안에서 오른쪽·아래쪽 여백을 제한한다. 아직 측정되지
 * 않은 지도에는 0 여백을 반환해 SDK에 유효하지 않은 padding을 전달하지 않는다.
 */
internal fun SairoMapViewportPaddingPx.clampToViewport(
    viewportWidth: Int,
    viewportHeight: Int,
): SairoMapViewportPaddingPx {
    if (viewportWidth <= 0 || viewportHeight <= 0) return SairoMapViewportPaddingPx()

    val clampedLeft = left.coerceIn(0, viewportWidth)
    val clampedTop = top.coerceIn(0, viewportHeight)

    return SairoMapViewportPaddingPx(
        left = clampedLeft,
        top = clampedTop,
        right = right.coerceIn(0, viewportWidth - clampedLeft),
        bottom = bottom.coerceIn(0, viewportHeight - clampedTop),
    )
}

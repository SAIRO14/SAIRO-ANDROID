package com.example.sairo14.feature.home

/** 홈 캔버스에서 카드가 배치될 가로·세로 기준점이다. */
internal enum class HomeCanvasAnchor(
    internal val fraction: Float,
) {
    Start(0f),
    Center(0.5f),
    End(1f),
}

/** px 단위로 계산하는 홈 캔버스의 가로·세로 크기다. */
internal data class HomeCanvasSize(
    val width: Float,
    val height: Float,
) {
    init {
        require(width > 0f) { "캔버스 너비는 0보다 커야 합니다." }
        require(height > 0f) { "캔버스 높이는 0보다 커야 합니다." }
    }
}

/** px 단위의 홈 캔버스 사각형 영역이다. */
internal data class HomeCanvasRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    init {
        require(right >= left) { "right는 left보다 작을 수 없습니다." }
        require(bottom >= top) { "bottom은 top보다 작을 수 없습니다." }
    }

    /** 이 사각형의 모든 변을 [amount]만큼 확장한다. */
    fun expand(amount: Float): HomeCanvasRect {
        require(amount >= 0f) { "시각적 여유는 음수일 수 없습니다." }
        return HomeCanvasRect(
            left = left - amount,
            top = top - amount,
            right = right + amount,
            bottom = bottom + amount,
        )
    }
}

/** 홈 카드 하나의 기준점·추가 오프셋을 캔버스 좌표로 변환하는 배치 정의다. */
internal data class HomeSavedTripPlacement(
    val horizontalAnchor: HomeCanvasAnchor,
    val verticalAnchor: HomeCanvasAnchor,
    val offsetX: Float,
    val offsetY: Float,
) {
    /** 카드 크기와 캔버스 크기를 이용해 카드의 회전 전 사각형을 계산한다. */
    fun resolveBounds(
        canvasSize: HomeCanvasSize,
        cardSize: HomeCanvasSize,
    ): HomeCanvasRect {
        val left = (canvasSize.width - cardSize.width) * horizontalAnchor.fraction + offsetX
        val top = (canvasSize.height - cardSize.height) * verticalAnchor.fraction + offsetY
        return HomeCanvasRect(
            left = left,
            top = top,
            right = left + cardSize.width,
            bottom = top + cardSize.height,
        )
    }
}

/** 홈 캔버스에 저장 최신순으로 적용할 최대 여덟 개의 카드 배치를 제공한다. */
internal object HomeSavedTripPlacements {

    /** 카드 크기에 비례해 카드 사이의 여유를 유지하는 최대 여덟 개 슬롯을 만든다. */
    fun create(cardSize: HomeCanvasSize): List<HomeSavedTripPlacement> {
        val horizontalGap = cardSize.width * (CardGapAtReferenceSize / ReferenceCardWidth)
        val verticalGap = cardSize.height * (CardGapAtReferenceSize / ReferenceCardHeight)

        return listOf(
            HomeSavedTripPlacement(
                horizontalAnchor = HomeCanvasAnchor.Start,
                verticalAnchor = HomeCanvasAnchor.Start,
                offsetX = cardSize.width * -0.5f,
                offsetY = cardSize.height * -0.2f,
            ),
            HomeSavedTripPlacement(
                horizontalAnchor = HomeCanvasAnchor.End,
                verticalAnchor = HomeCanvasAnchor.Start,
                offsetX = cardSize.width * 1.06f,
                offsetY = cardSize.height * -(1f / 65f),
            ),
            HomeSavedTripPlacement(
                horizontalAnchor = HomeCanvasAnchor.End,
                verticalAnchor = HomeCanvasAnchor.End,
                offsetX = cardSize.width * (113f / ReferenceCardWidth),
                offsetY = cardSize.height * (116f / 195f),
            ),
            HomeSavedTripPlacement(
                horizontalAnchor = HomeCanvasAnchor.Start,
                verticalAnchor = HomeCanvasAnchor.End,
                offsetX = cardSize.width * -0.28f,
                offsetY = cardSize.height * (49f / 195f),
            ),
            HomeSavedTripPlacement(
                horizontalAnchor = HomeCanvasAnchor.Start,
                verticalAnchor = HomeCanvasAnchor.Center,
                offsetX = cardSize.width * -1.5f - horizontalGap,
                offsetY = cardSize.height * (6f / 195f),
            ),
            HomeSavedTripPlacement(
                horizontalAnchor = HomeCanvasAnchor.End,
                verticalAnchor = HomeCanvasAnchor.Center,
                offsetX = cardSize.width * 1.24f,
                offsetY = cardSize.height * (54f / 195f),
            ),
            HomeSavedTripPlacement(
                horizontalAnchor = HomeCanvasAnchor.Center,
                verticalAnchor = HomeCanvasAnchor.Start,
                offsetX = cardSize.width * (38f / ReferenceCardWidth),
                offsetY = cardSize.height * -(171f / 195f),
            ),
            HomeSavedTripPlacement(
                horizontalAnchor = HomeCanvasAnchor.Center,
                verticalAnchor = HomeCanvasAnchor.End,
                offsetX = 0f,
                offsetY = cardSize.height + cardSize.height * (49f / 195f) + verticalGap,
            ),
        )
    }

    private const val ReferenceCardWidth = 150f
    private const val ReferenceCardHeight = 195f
    private const val CardGapAtReferenceSize = 48f
}

/** 캔버스 Offset에 적용할 비대칭 가로·세로 이동 한계다. */
internal data class HomeCanvasPanBounds(
    val minX: Float = 0f,
    val maxX: Float = 0f,
    val minY: Float = 0f,
    val maxY: Float = 0f,
) {
    init {
        require(minX <= maxX) { "가로 이동 한계의 최소값이 최대값보다 클 수 없습니다." }
        require(minY <= maxY) { "세로 이동 한계의 최소값이 최대값보다 클 수 없습니다." }
    }
}

/** 카드 원본 위치와 회전·그림자를 고려한 시각적 범위를 함께 보관한다. */
internal data class HomeResolvedSavedTripPlacement(
    val placement: HomeSavedTripPlacement,
    val cardBounds: HomeCanvasRect,
    val visualBounds: HomeCanvasRect,
)

/** 카드 배치와 해당 카드들을 탐색할 수 있는 이동 범위를 묶은 계산 결과다. */
internal data class HomeCanvasLayout(
    val cardPlacements: List<HomeResolvedSavedTripPlacement>,
    val panBounds: HomeCanvasPanBounds,
)

/** 홈 저장 여행지 카드 배치와 캔버스 이동 한계를 계산한다.
 *
 * 입력·출력은 모두 px 단위의 순수 값 객체다. Compose는 Density 변환과 실제 카드 렌더링만 담당하며,
 * 이 정책은 활성 카드의 시각적 외곽이 보이는 영역까지 이동할 수 있는 최소 범위를 결정한다.
 */
internal object HomeCanvasLayoutPolicy {

    /** 활성 카드의 실제 배치와 드래그 한계를 계산한다.
     *
     * [cardCount]는 제공한 [placements] 수를 넘을 수 없으며, 0이면 이동 한계가 없는 빈 결과를 반환한다.
     * [visibleViewport]에는 고정 헤더와 시스템 인셋을 제외한 사용자가 카드를 볼 수 있는 영역을 전달한다.
     * @param canvasSize 카드가 이동하는 전체 캔버스 크기
     * @param visibleViewport 고정 UI를 제외한 실제 가시 영역
     * @param cardSize 회전 전 카드의 고정 크기
     * @param placements 저장 최신순으로 사용할 카드 슬롯 정의
     * @param cardCount 현재 저장된 카드 수
     * @param revealPadding 카드 가장자리와 가시 영역 사이에 남길 여유
     * @param visualOverflow 회전·그림자를 고려해 카드 사각형 밖으로 확장할 여유
     */
    fun calculate(
        canvasSize: HomeCanvasSize,
        visibleViewport: HomeCanvasRect,
        cardSize: HomeCanvasSize,
        placements: List<HomeSavedTripPlacement>,
        cardCount: Int,
        revealPadding: Float = 0f,
        visualOverflow: Float = 0f,
    ): HomeCanvasLayout {
        require(revealPadding >= 0f) { "카드 노출 여유는 음수일 수 없습니다." }
        require(visualOverflow >= 0f) { "카드 시각 여유는 음수일 수 없습니다." }

        val activePlacements = placements.take(cardCount.coerceAtLeast(0))
            .map { placement ->
                val cardBounds = placement.resolveBounds(canvasSize, cardSize)
                HomeResolvedSavedTripPlacement(
                    placement = placement,
                    cardBounds = cardBounds,
                    visualBounds = cardBounds.expand(visualOverflow),
                )
            }

        if (activePlacements.isEmpty()) {
            return HomeCanvasLayout(
                cardPlacements = emptyList(),
                panBounds = HomeCanvasPanBounds(),
            )
        }

        val contentBounds = activePlacements.map(HomeResolvedSavedTripPlacement::visualBounds)
            .reduce(::union)
        return HomeCanvasLayout(
            cardPlacements = activePlacements,
            panBounds = HomeCanvasPanBounds(
                minX = minOf(0f, visibleViewport.right - revealPadding - contentBounds.right),
                maxX = maxOf(0f, visibleViewport.left + revealPadding - contentBounds.left),
                minY = minOf(0f, visibleViewport.bottom - revealPadding - contentBounds.bottom),
                maxY = maxOf(0f, visibleViewport.top + revealPadding - contentBounds.top),
            ),
        )
    }

    private fun union(first: HomeCanvasRect, second: HomeCanvasRect): HomeCanvasRect = HomeCanvasRect(
        left = minOf(first.left, second.left),
        top = minOf(first.top, second.top),
        right = maxOf(first.right, second.right),
        bottom = maxOf(first.bottom, second.bottom),
    )
}

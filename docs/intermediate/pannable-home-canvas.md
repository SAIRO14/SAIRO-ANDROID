# 이동 가능한 홈 캔버스

## 개념

이동 가능한 캔버스는 화면 전체를 스크롤 목록으로 바꾸지 않고, 한 화면 안의 콘텐츠 묶음만 드래그한 거리만큼 이동시키는 Compose UI 패턴이다. Home에서는 저장 여행지 카드와 중앙 CTA가 캔버스에 속하고 헤더만 화면에 고정된다.

## 도입 이유

저장한 여행지가 있을 때 네 장의 카드가 화면 가장자리에서 일부만 보이는 Figma 구성을 재현하려면 일반적인 세로 목록보다 자유로운 2차원 이동이 필요하다. 반면 저장 여행지가 없는 화면은 드래그할 콘텐츠가 없으므로 기존의 고정된 빈 상태를 유지해야 한다.

## 프로젝트 적용

- 관련 파일: [`HomeScreen.kt`](../../app/src/main/java/com/example/sairo14/feature/home/HomeScreen.kt)
- 관련 파일: [`HomeSavedTripCard.kt`](../../app/src/main/java/com/example/sairo14/feature/home/HomeSavedTripCard.kt)
- 관련 파일: [`HomeUiState.kt`](../../app/src/main/java/com/example/sairo14/feature/home/HomeUiState.kt)
- 관련 파일: [`HomeCanvasLayoutPolicy.kt`](../../app/src/main/java/com/example/sairo14/feature/home/HomeCanvasLayoutPolicy.kt)

`HomePannableCanvas`는 `savedTrips`가 비어 있지 않을 때만 만들어진다. 저장 카드뿐 아니라 중앙 제목·탐색 CTA도 같은 캔버스 자식으로 배치하므로 드래그할 때 함께 움직인다. 드래그 결과는 ViewModel이 아닌 Composition의 `Offset` 상태로 소유한다. 화면을 다시 열 때 카드 각도가 새로 결정되는 UI 표현과 마찬가지로, 현재 화면 수명에만 필요한 상태이기 때문이다.

이동 한계는 고정 dp 값이 아니라 캔버스의 가로·세로 크기 비율로 계산한다. `SairoHeader`는 화면에 고정하되 같은 backdrop 캡처 계층 위에 둔다. 드래그 시작 시 한 번 즉시 캡처하고, 이후 이벤트는 다음 프레임에 한 번만 갱신한다. 첫 이동의 blur 지연을 줄이면서 구형 기기의 CPU blur 작업을 제한하는 방식이다.

```kotlin
detectDragGestures { change, dragAmount ->
    change.consume()
    canvasOffset = Offset(
        x = (canvasOffset.x + dragAmount.x).coerceIn(-limit, limit),
        y = (canvasOffset.y + dragAmount.y).coerceIn(-limit, limit),
    )
}
```

카드 위치는 화면의 네 `Alignment` 슬롯과 오프셋으로 정한다. 사진 카드 자체는 Figma 규격인 150×195dp를 유지하고, 각도만 카드 ID를 key로 한 Composition마다 한 번 무작위로 정한다.

저장 카드가 더 늘어날 때는 Compose에서 위치와 드래그 한계를 각각 계산하지 않는다. `HomeCanvasLayoutPolicy`는 px 단위의 캔버스·카드·가시 영역과 활성 카드 수를 받아, 카드의 원본 사각형과 회전·그림자 여유가 포함된 시각적 사각형, 비대칭 이동 한계를 함께 반환한다. 고정 헤더와 시스템 인셋을 제외한 영역은 `visibleViewport`로 전달하므로, 작은 화면에서도 실제로 가려지는 방향만큼만 이동할 수 있다.

```kotlin
val layout = HomeCanvasLayoutPolicy.calculate(
    canvasSize = canvasSize,
    visibleViewport = visibleViewport,
    cardSize = cardSize,
    placements = HomeSavedTripPlacements.create(cardSize),
    cardCount = savedTrips.size,
    visualOverflow = shadowAndRotationOverflow,
)
```

카드가 없으면 이동 한계는 모두 0이다. 카드가 화면 왼쪽 밖에만 있으면 오른쪽으로 되돌리는 이동만 허용하는 식으로, `minX..maxX`, `minY..maxY`를 독립적으로 계산한다. 이후 UI 연결 단계에서는 이 결과를 `canvasOffset.coerceIn(...)`과 카드 위치에 함께 사용한다.

## 흐름과 영향 범위

```mermaid
flowchart LR
    S["HomeUiState.Content"] --> L{"savedTrips 비어 있음?"}
    L -->|"예"| E["고정된 빈 Home"]
    L -->|"아니오"| C["HomePannableCanvas"]
    C --> D["드래그 Offset"]
    D --> P["카드와 중앙 CTA 이동"]
    C --> H["고정 Header의 backdrop blur 갱신"]
```

## 트레이드오프와 주의점

- 이동 범위는 제한한다. 제한이 없으면 사용자가 카드를 모두 화면 밖으로 옮겨 현재 위치를 잃을 수 있다.
- 현재 Figma 설계에 맞춰 최대 네 장만 렌더링한다. 저장 여행지가 더 많아지는 제품 요구가 확정되면 슬롯 배치나 페이지 전략을 별도로 설계해야 한다.
- 배치 정책은 현재 UI에 아직 연결하지 않는다. 렌더링 단계에서 정책의 사각형과 이동 한계를 함께 적용해야 위치와 탐색 범위가 어긋나지 않는다.
- 카드 회전은 재구성마다 바뀌지 않지만, 화면을 새로 만들면 달라진다. 저장된 사용자 설정처럼 영속할 값이 아니므로 Domain 모델에 넣지 않는다.
- 카드 클릭과 드래그는 같은 영역을 공유한다. 드래그 이동이 없는 탭은 카드 클릭으로 전달되며, 이동 거리만 캔버스가 소비한다.
- blur 갱신은 드래그 시작 시 즉시 한 번, 이후 프레임당 한 번으로 제한한다. blur를 드래그 종료 후에만 갱신하면 움직이는 캔버스와 헤더 표현이 분리되어 보이므로, 프레임 단위 갱신을 유지한다.

## 추가 학습 및 대안

현재는 확대·축소 없이 드래그만 지원한다. 지도처럼 넓은 영역을 탐색해야 한다면 `transformable`로 scale까지 상태에 포함할 수 있다.

> 아래 예시는 현재 프로젝트에 적용되지 않은 확대·축소 대안이다.

```kotlin
val transformState = rememberTransformableState { zoomChange, panChange, _ ->
    zoom *= zoomChange
    offset += panChange
}

Box(Modifier.transformable(transformState)) {
    // scale과 offset을 함께 적용한 캔버스
}
```

이 방식은 카드의 최소 터치 영역, 캡션 글자 크기, 헤더 blur 캡처 범위를 함께 고려해야 하므로 현재 네 슬롯 Home에는 적용하지 않는다.

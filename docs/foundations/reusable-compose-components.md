# 재사용 가능한 Compose 컴포넌트

## 개념

재사용 컴포넌트는 여러 화면에서 같은 역할과 규격을 갖는 UI를 하나의 공개 API로 제공하는 방식이다. 화면은 문구와 클릭 동작을 전달하고, 컴포넌트는 크기, 색상, 상태 변화처럼 반복되는 표현 규칙을 책임진다.

## 도입 이유

주요 CTA를 화면마다 직접 구현하면 버튼 크기나 비활성 색상이 달라지기 쉽고, 디자인 변경 때 모든 화면을 찾아 수정해야 한다. `SairoButton`은 Figma의 L/M/S, 기본/눌림/비활성, Primary/Outline 조합을 한 곳에서 관리한다.

## 프로젝트 적용

- 관련 파일: [`SairoButton.kt`](../../app/src/main/java/com/example/sairo14/core/designsystem/component/SairoButton.kt)

화면은 필요한 크기와 스타일만 선택하고, 문구와 동작을 전달한다.

```kotlin
SairoButton(
    text = stringResource(R.string.next),
    onClick = onNextClick,
    modifier = Modifier.fillMaxWidth(),
    size = SairoButtonSize.Large,
)
```

색상은 원시 색상이 아닌 `SairoTheme.colors`의 `actionDefault`, `actionOutlineBorder` 같은 시맨틱 토큰을 사용한다. 글자 크기는 `SairoTextStyles.headRegular20`과 `headRegular18`을 사용해 Figma의 타입 스케일과 맞춘다.

정보 표시용 태그는 [`SairoTag.kt`](../../app/src/main/java/com/example/sairo14/core/designsystem/component/SairoTag.kt)에서 제공한다. Figma에는 Medium/Lemon과 Small의 Lemon·Gray·White만 정의되어 있으므로, `size`와 `color`를 독립된 인자로 받지 않고 지원되는 네 가지 조합을 `SairoTagVariant`로 표현한다.

이미지 선택 카드인 [`SairoImageCard.kt`](../../app/src/main/java/com/example/sairo14/core/designsystem/component/SairoImageCard.kt)는 이미지를 nullable `Painter`로 받고 `selected`만 표현한다. `painter`가 `null`이면 surface 배경을 유지한 채 선택 결과의 테두리·gradient·체크 아이콘만 그린다. 크기는 `SairoImageCardSize.Large`(300×400dp)와 `Medium`(260×347dp) 중 선택하며, 기본값은 Large다. `onClick`이 전달된 카드만 클릭 가능하고, 카드가 어떤 사진을 선택할지와 선택 변경 동작은 화면 또는 ViewModel이 소유한다.

선택 가능한 공통 chip은 [`SairoChip.kt`](../../app/src/main/java/com/example/sairo14/core/designsystem/component/SairoChip.kt)에서 `selected`와 `onClick`을 분리한다. 여행 일차처럼 화면은 선택 상태를 소유하고, 각 chip에는 `selected = item == selectedItem`을 전달한다. chip은 선택 표현과 RadioButton 접근성 semantics만 책임진다.

## 흐름과 영향 범위

`enabled = false`이면 비활성 배경과 글자색이 적용되고 클릭 이벤트가 차단된다. 활성 버튼은 `MutableInteractionSource`에서 실제 터치 눌림 상태를 관찰해 Primary는 `actionPressed`, Outline은 `actionOutlineBackgroundPressed`로 바뀐다. 따라서 화면이 일시적인 눌림 상태를 별도로 저장할 필요가 없다.

## 트레이드오프와 주의점

- 버튼은 내용 크기에 맞춰지므로 전체 너비 CTA가 필요하면 호출부에서 `Modifier.fillMaxWidth()`를 전달해야 한다.
- `Small`은 Figma 규격상 40dp 높이다. 작은 버튼은 충분한 주변 여백을 두고, 중요한 동작에는 Large 또는 Medium을 우선 사용해 터치하기 쉽게 만든다.
- 눌림 색상을 화면 상태로 직접 전달하지 않는다. 실제 입력과 분리되면 키보드·터치 상호작용의 상태가 어긋날 수 있다.
- 태그 variant를 하나의 enum으로 제한하면 잘못된 조합을 컴파일 단계에서 막을 수 있지만, Figma가 Medium/Gray 같은 새 조합을 추가하면 enum과 프리뷰를 함께 보완해야 한다.
- `SairoImageCard`는 클릭 이벤트를 받지 않는다. 카드 선택 방식(탭, 여러 장 선택, 필수 선택 여부)은 화면 흐름마다 다를 수 있으므로, 해당 정책이 확정된 화면에서 클릭 처리와 `selected` 상태를 연결한다.

## 추가 학습 및 대안

현재는 문구만 표시하는 CTA를 지원한다. 아이콘 버튼이 필요해지면 문자열 전용 API를 무리하게 확장하기보다, 아이콘의 의미를 접근성에 전달할 수 있는 별도 컴포넌트를 만든다.

이미지 카드의 사진 입력은 현재 `Painter`로 받는다. 로컬 drawable을 사용하는 경우에는 간단하지만, URL 이미지의 로딩·오류 표현까지 카드가 지원해야 한다면 `Painter` API를 없애기보다 이미지 슬롯 API를 추가하는 방식을 권장한다. 호출부는 Coil의 `AsyncImage` 또는 로컬 `Image`를 선택하고, 카드 컴포넌트는 선택 테두리·gradient·체크 표시만 일관되게 책임진다.

> 아래 예시는 현재 프로젝트에 적용되지 않은 대안이다.

```kotlin
@Composable
fun SairoImageCard(
    selected: Boolean,
    image: @Composable BoxScope.() -> Unit,
) {
    Box {
        image()
        if (selected) {
            // 선택 테두리와 체크 표시를 공통으로 그림
        }
    }
}
```

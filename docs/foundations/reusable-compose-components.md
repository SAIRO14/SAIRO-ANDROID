# 재사용 가능한 Compose 컴포넌트

## 개념

재사용 컴포넌트는 여러 화면에서 같은 역할과 규격을 갖는 UI를 하나의 공개 API로 제공하는 방식이다. 화면은 문구와 클릭 동작을 전달하고, 컴포넌트는 크기, 색상, 상태 변화처럼 반복되는 표현 규칙을 책임진다.

## 도입 이유

주요 CTA를 화면마다 직접 구현하면 버튼 크기나 비활성 색상이 달라지기 쉽고, 디자인 변경 때 모든 화면을 찾아 수정해야 한다. `SairoButton`은 Figma의 L/M/S, 기본/비활성, Primary/Outline 조합을 한 곳에서 관리한다.

## 프로젝트 적용

- 관련 파일: [`SairoButton.kt`](../../app/src/main/java/com/example/sairo14/core/designsystem/component/SairoButton.kt)
- 관련 파일: [`ModifierExt.kt`](../../app/src/main/java/com/example/sairo14/core/extension/ModifierExt.kt)

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

클릭 가능한 공통 컴포넌트는 `Modifier.noRippleClickable()`을 사용한다. 이 확장 함수는 클릭과
접근성 역할은 유지하고, 터치 시 ripple·눌림 색상 같은 일시적 시각 효과를 표시하지 않는다.

정보 표시용 태그는 [`SairoTag.kt`](../../app/src/main/java/com/example/sairo14/core/designsystem/component/SairoTag.kt)에서 제공한다. Figma에는 Medium/Lemon과 Small의 Lemon·Gray·White만 정의되어 있으므로, `size`와 `color`를 독립된 인자로 받지 않고 지원되는 네 가지 조합을 `SairoTagVariant`로 표현한다.

이미지 선택 카드인 [`SairoImageCard.kt`](../../app/src/main/java/com/example/sairo14/core/designsystem/component/SairoImageCard.kt)는 이미지를 nullable `Painter`로 받고 `selected`만 표현한다. `painter`가 `null`이면 surface 배경을 유지한 채 선택 결과의 테두리·gradient·체크 아이콘만 그린다. 크기는 `SairoImageCardSize.Large`(300×400dp)와 `Medium`(260×347dp) 중 선택하며, 기본값은 Large다. `onClick`이 전달된 카드만 클릭 가능하고, 카드가 어떤 사진을 선택할지와 선택 변경 동작은 화면 또는 ViewModel이 소유한다.

선택 가능한 공통 chip은 [`SairoChip.kt`](../../app/src/main/java/com/example/sairo14/core/designsystem/component/SairoChip.kt)에서 `selected`와 `onClick`을 분리한다. 여행 일차처럼 화면은 선택 상태를 소유하고, 각 chip에는 `selected = item == selectedItem`을 전달한다. chip은 선택 표현과 RadioButton 접근성 semantics만 책임진다.

폴더 프레임은 [`SairoFolderFrame.kt`](../../app/src/main/java/com/example/sairo14/core/designsystem/component/SairoFolderFrame.kt)에서 Large·Medium·Small 시각 변형의 배경 실루엣만 제공한다. 실제 크기는 부모의 제약과 `Modifier`가 결정한다. 홈 CTA와 저장 여행지 카드처럼 내부 콘텐츠와 클릭 구조가 달라지는 경우, 화면 또는 feature Composable이 `Box`에서 프레임 위에 카드·버튼·정보를 배치한다.

이미지 삭제용 썸네일은 [`SairoImageThumbnail.kt`](../../app/src/main/java/com/example/sairo14/core/designsystem/component/SairoImageThumbnail.kt)에서 이미지는 `Painter`, 삭제 동작은 `onRemoveClick`으로 분리한다. X 버튼은 시각적으로 썸네일 바깥으로 돌출되므로, 부모 레이아웃이 필요한 여백을 소유한다.

여행 상세 바텀시트의 장소 행은 [`SairoPlaceListItem.kt`](../../app/src/main/java/com/example/sairo14/core/designsystem/component/SairoPlaceListItem.kt)에서 `Simple`과 `Detailed` 정보 배치를 [SairoPlaceListItemVariant]로 분리한다. 태그는 새로 구현하지 않고 기존 `SairoTag`의 `SmallGray` 변형을 조합한다.

저장 여행지 폴더 카드는 [`SairoPlaceFolderCard.kt`](../../app/src/main/java/com/example/sairo14/core/designsystem/component/SairoPlaceFolderCard.kt)에서 `SairoFolderFrame`, `SairoTag`, `SairoBookmarker`를 조합한다. 카드 이동과 저장 상태는 호출자가 소유하고, 카드·북마크 클릭 콜백을 각각 전달한다. 이 카드는 항상 Small 폴더 변형을 사용하며, 겹친 사진 레이아웃은 전달받은 이미지 목록의 첫 두 장과 부모 너비 비율을 사용한다.

공통 헤더는 [`SairoHeader.kt`](../../app/src/main/java/com/example/sairo14/core/designsystem/component/SairoHeader.kt)에서 Home·Sub·SubFilled 변형을 제공한다. 헤더가 상태 표시줄 inset을 직접 처리하고, 화면은 제목과 뒤로가기·우측 액션 결과를 소유한다. 실제 backdrop blur가 필요한 Home·Sub 화면은 Cloudy의 `rememberSky()`로 만든 `Sky`를 콘텐츠의 `Modifier.sky(sky)`와 헤더의 `backdropSky`에 함께 전달한다. `SubFilled`는 불투명 표면이므로 blur를 적용하지 않는다.

## 흐름과 영향 범위

`enabled = false`이면 비활성 배경과 글자색이 적용되고 클릭 이벤트가 차단된다. 활성 버튼과 카드의
클릭은 `noRippleClickable`이 처리하므로 화면은 일시적인 눌림 상태를 저장하지 않는다. 선택 여부처럼
사용자에게 지속적으로 보여야 하는 상태만 ViewModel 또는 호출 화면이 소유한다.

## 트레이드오프와 주의점

- 버튼은 내용 크기에 맞춰지므로 전체 너비 CTA가 필요하면 호출부에서 `Modifier.fillMaxWidth()`를 전달해야 한다.
- `Small`은 Figma 규격상 40dp 높이다. 작은 버튼은 충분한 주변 여백을 두고, 중요한 동작에는 Large 또는 Medium을 우선 사용해 터치하기 쉽게 만든다.
- ripple을 제거하면 즉각적인 터치 피드백이 줄어든다. 선택 상태·로딩·화면 이동처럼 동작 결과를
  분명하게 보여 주고, 아이콘 전용 동작에는 접근성 역할과 설명을 제공해야 한다.
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

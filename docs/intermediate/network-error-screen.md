# 네트워크 오류 화면의 상태 소유

## 개념

네트워크 오류 화면은 실패한 요청을 다시 실행하거나 홈으로 이동할 수 있게 하는 공통 전체 화면 UI다. 화면의 모양은 공통으로 재사용하지만, 어떤 요청이 실패했는지와 재시도 중인지의 상태는 원래 요청을 시작한 Feature ViewModel이 소유한다.

## 도입 이유

오류 화면이 자체 ViewModel에서 재시도 대상을 기억하면 화면마다 다른 UseCase와 입력값을 전달해야 하고, Nav3 백스택 전환 중 원래 요청 문맥을 잃기 쉽다. 공통 화면을 stateless하게 두면 Home, 온보딩, 이후의 API 화면이 같은 UI를 쓰면서도 각자 정확한 요청을 재시도할 수 있다.

## 프로젝트 적용

- 공통 Route와 Screen: [`NetworkErrorScreen.kt`](../../app/src/main/java/com/example/sairo14/feature/error/NetworkErrorScreen.kt)
- Home 오류 연결과 중복 재시도 방지: [`HomeScreen.kt`](../../app/src/main/java/com/example/sairo14/feature/home/HomeScreen.kt), [`HomeViewModel.kt`](../../app/src/main/java/com/example/sairo14/feature/home/HomeViewModel.kt)

`NetworkErrorRoute`는 콜백을 `NetworkErrorScreen`으로 전달한다. `NetworkErrorScreen`은 버튼 클릭을 처리하지만 네트워크 요청을 직접 실행하거나 상태를 변경하지 않는다.

```kotlin
NetworkErrorRoute(
    onRetryClick = viewModel::retry,
    onHomeClick = navigator::popToHome,
)
```

## 흐름과 영향 범위

```mermaid
flowchart LR
    VM["Feature ViewModel"] -->|"Error 상태"| Route["NetworkErrorRoute"]
    Route --> Screen["NetworkErrorScreen"]
    Screen -->|"재시도"| VM
    Screen -->|"홈 이동"| Nav["SairoNavigator"]
```

Home은 이미 홈에 있으므로 `showHomeAction = false`로 홈 이동 버튼을 숨긴다. 다른 목적지에서는 기본값을 사용하고 `SairoNavigator.popToHome()`을 연결한다.

화면은 `WindowInsets.safeDrawing` 안에서 일반 높이에는 메시지를 중앙, 버튼을 하단에 배치한다. 작은 높이에서는 세로 스크롤 Column으로 전환해 큰 글꼴이나 가로 모드에서도 겹치지 않게 한다.

## 트레이드오프와 주의점

stateless 화면은 재사용하기 쉽지만, 호출한 Feature가 재시도 중 상태를 즉시 `Loading`으로 바꾸지 않으면 연속 탭으로 중복 요청이 생길 수 있다. Home은 진행 중 `Job`을 확인하고, 새 요청을 시작하기 전에 동기적으로 `Loading`을 설정해 이를 막는다.

[`AppError`](../../app/src/main/java/com/example/sairo14/domain/model/AppResult.kt)는 네트워크 연결 불가, 잘못된 요청·커서, 찾을 수 없는 리소스, 충돌, 서버 실패, 저장소 실패를 구분한다. [`RemoteErrorMapper.kt`](../../app/src/main/java/com/example/sairo14/data/remote/RemoteErrorMapper.kt)의 `runRemoteOperation`이 실제 Retrofit 호출을 이 계약으로 변환하고, `CancellationException`은 다시 던진다. `IOException`의 하위 타입인 시간 초과도 `NetworkUnavailable`로 통일한다. 화면은 이 오류에만 공통 네트워크 오류 화면을 사용하며, 서버·저장소 오류는 일반 오류 UI를 표시한다.

[`AndroidNetworkStatusRepository`](../../app/src/main/java/com/example/sairo14/core/network/AndroidNetworkStatusRepository.kt)는 `ConnectivityManager`의 검증된 인터넷 연결 상태를 `Flow`로 제공한다. 이 값은 실제 서버 요청의 성공을 보장하지 않으므로 요청을 미리 차단하지 않고, 연결 복구 안내 같은 보조 UI에만 사용한다.

## 추가 학습 및 대안

오류 화면을 별도 Nav3 목적지로 만들 수도 있다. 하지만 route에는 suspend 요청이나 콜백을 저장할 수 없어 재시도 문맥이 복잡해진다.

> 아래 예시는 현재 프로젝트에 적용하지 않은 대안이다.

```kotlin
@Serializable
data object NetworkErrorRoute : SairoRoute
```

이 방식은 오류 원인과 재시도 동작을 공유 상태나 별도 저장소로 옮겨야 하므로, 현재처럼 Feature `UiState`가 화면을 교체하는 구조보다 복잡하다.

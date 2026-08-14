package com.example.sairo14.feature.bookmark

import androidx.compose.runtime.Immutable
import com.example.sairo14.domain.model.AppError

/** 서버 응답을 바탕으로 한 화면의 북마크 표시와 요청 상태를 표현한다.
 *
 * [isSaved]는 아이콘 표시를 결정하고, [savedTripId]는 저장 해제 요청에만 사용한다. 이 상태는 화면
 * 생명주기 동안만 유지하며, 화면을 새로 구성할 때는 서버가 반환한 저장 상태로 초기화한다.
 * @param isSaved 현재 서버 응답에 따른 저장 표시 여부
 * @param savedTripId 저장 해제 API에 전달할 저장 항목 ID. 아직 알 수 없으면 `null`
 * @param isRequesting 저장 또는 해제 요청이 진행 중인지 여부
 */
@Immutable
data class BookmarkUiState(
    val isSaved: Boolean = false,
    val savedTripId: String? = null,
    val isRequesting: Boolean = false,
) {
    /** 같은 코스의 중복 북마크 요청을 막기 위해 카드 상세 이동을 허용하는지 나타낸다. */
    val isDetailNavigationEnabled: Boolean
        get() = !isRequesting
}

/** 북마크 요청 실패를 한 번만 화면에 알리는 효과다.
 *
 * 오류 문구 표시와 재시도 행동은 화면이 소유하며, 이 효과는 [BookmarkUiState]를 변경하지 않는다.
 */
sealed interface BookmarkEffect {

    /** 저장 또는 해제 요청이 실패해 사용자 안내가 필요한 상태다.
     * @param error Data 계층이 Domain 오류로 변환한 실패 원인
     */
    data class ShowError(
        val error: AppError,
    ) : BookmarkEffect
}

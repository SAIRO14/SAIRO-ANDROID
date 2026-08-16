package com.example.sairo14.feature.savedtrip

import androidx.compose.runtime.Immutable
import com.example.sairo14.domain.model.AppError

/** 저장된 여행지 목록 화면이 표시할 조회 상태를 나타낸다. */
sealed interface SavedTripsUiState {

    /** 익명 사용자 식별자와 저장 목록을 준비하는 중이다. */
    data object Loading : SavedTripsUiState

    /** 저장된 여행지 카드 목록을 표시할 수 있는 상태다. */
    @Immutable
    data class Content(
        val trips: List<SavedTripUiModel>,
        val nextCursor: String?,
        val isLoadingMore: Boolean = false,
        val loadMoreError: AppError? = null,
        val removingSavedTripIds: Set<String> = emptySet(),
    ) : SavedTripsUiState

    /** 저장된 여행지가 없어 탐색 CTA를 표시하는 상태다. */
    data object Empty : SavedTripsUiState

    /** 익명 사용자 식별자 또는 저장 목록을 읽지 못해 재시도가 필요한 상태다. */
    data object Error : SavedTripsUiState
}

/** 저장 목록 폴더 카드에 전달할 UI 전용 여행지 정보다.
 *
 * 이미지와 장소 목록은 서버 응답이 비었을 때 ViewModel이 기존 대표 정보로 보완해 전달한다.
 * @param spotNames 카드 하단에 표시할 장소명 목록
 * @param imageUrls 겹쳐 표시할 여행지 이미지 주소 목록
 */
@Immutable
data class SavedTripUiModel(
    val savedTripId: String,
    val courseId: String,
    val regionName: String,
    val reason: String?,
    val spotNames: List<String>,
    val imageUrls: List<String>,
)

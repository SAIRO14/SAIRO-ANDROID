package com.example.sairo14.feature.savedtrip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SavedTrip
import com.example.sairo14.domain.model.SavedTripPage
import com.example.sairo14.domain.usecase.DeleteSavedTripUseCase
import com.example.sairo14.domain.usecase.GetSavedTripsUseCase
import com.example.sairo14.feature.bookmark.BookmarkChange
import com.example.sairo14.feature.bookmark.BookmarkChangeNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 저장된 여행지 목록의 조회 상태를 관리하고 재시도를 처리한다.
 *
 * 최초·추가 조회 결과를 [SavedTripsUiState]로 변환하고, 북마크 해제에 성공한 카드를 목록에서 제거한다.
 * 추가 조회는 서버 커서와 진행 상태를 이 ViewModel이 소유해 중복 요청을 막는다. 기기 식별과 API 헤더
 * 준비는 Repository 구현이 담당한다. 상세 화면의 저장 해제 성공은 [BookmarkChangeNotifier]를 통해
 * 반영하며, 이 화면에서 해제한 결과도 같은 통지자로 다른 화면에 전달한다. 화면 이동은 화면 호출자가
 * 소유한다.
 */
@HiltViewModel
class SavedTripsViewModel @Inject constructor(
    private val getSavedTrips: GetSavedTripsUseCase,
    private val deleteSavedTrip: DeleteSavedTripUseCase,
    private val bookmarkChangeNotifier: BookmarkChangeNotifier,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SavedTripsUiState>(SavedTripsUiState.Loading)
    private var pageRequestGeneration = 0L

    val uiState: StateFlow<SavedTripsUiState> = _uiState.asStateFlow()

    init {
        observeBookmarkChanges()
        loadSavedTrips()
    }

    /** 현재 오류 상태에 맞춰 첫 페이지 또는 실패한 다음 페이지를 다시 조회한다. */
    fun retry() {
        when (_uiState.value) {
            is SavedTripsUiState.Error -> loadSavedTrips()
            is SavedTripsUiState.Content -> {
                val content = _uiState.value as SavedTripsUiState.Content
                when (content.loadMoreError) {
                    AppError.InvalidCursor -> loadSavedTrips()
                    null -> Unit
                    else -> loadMore()
                }
            }
            else -> Unit
        }
    }

    /** 다음 페이지가 있고 진행 중이 아닐 때만 저장 여행지 목록을 추가 조회한다. */
    fun loadMore() {
        val content = _uiState.value as? SavedTripsUiState.Content ?: return
        val cursor = content.nextCursor ?: return
        if (content.isLoadingMore) return
        val requestGeneration = pageRequestGeneration

        _uiState.update { state ->
            val currentContent = state as? SavedTripsUiState.Content ?: return@update state
            if (currentContent.nextCursor != cursor || currentContent.isLoadingMore) {
                currentContent
            } else {
                currentContent.copy(
                    isLoadingMore = true,
                    loadMoreError = null,
                )
            }
        }

        viewModelScope.launch {
            when (val result = getSavedTrips(cursor = cursor)) {
                is AppResult.Success -> appendSavedTrips(cursor, requestGeneration, result.value)
                is AppResult.Failure -> {
                    if (result.error == AppError.InvalidCursor) {
                        loadSavedTrips()
                    } else {
                        setLoadMoreError(cursor, requestGeneration, result.error)
                    }
                }
            }
        }
    }

    /** 북마크 해제 요청이 성공하면 해당 카드를 목록에서 제거하고 다른 화면에 변경을 알린다. */
    fun removeSavedTrip(savedTripId: String) {
        val content = _uiState.value as? SavedTripsUiState.Content ?: return
        val trip = content.trips.firstOrNull { it.savedTripId == savedTripId } ?: return

        if (savedTripId in content.removingSavedTripIds) return

        _uiState.update { state ->
            val currentContent = state as? SavedTripsUiState.Content ?: return@update state
            currentContent.copy(
                removingSavedTripIds = currentContent.removingSavedTripIds + savedTripId,
            )
        }

        viewModelScope.launch {
            when (deleteSavedTrip(savedTripId)) {
                is AppResult.Success -> {
                    if (removeSavedTripsFromState { trip -> trip.savedTripId == savedTripId }) {
                        refreshAfterRemoval()
                    }
                    bookmarkChangeNotifier.notify(
                        BookmarkChange(
                            courseId = trip.courseId,
                            isSaved = false,
                            savedTripId = null,
                        ),
                    )
                }
                is AppResult.Failure -> clearRemovingState(savedTripId)
            }
        }
    }

    private fun loadSavedTrips() {
        val requestGeneration = ++pageRequestGeneration
        viewModelScope.launch {
            _uiState.value = SavedTripsUiState.Loading

            val nextState = when (val result = getSavedTrips()) {
                is AppResult.Failure -> SavedTripsUiState.Error(result.error)
                is AppResult.Success -> result.value.toUiState()
            }
            if (requestGeneration == pageRequestGeneration) _uiState.value = nextState
        }
    }

    private fun appendSavedTrips(
        cursor: String,
        requestGeneration: Long,
        page: SavedTripPage,
    ) {
        _uiState.update { state ->
            val content = state as? SavedTripsUiState.Content ?: return@update state
            if (requestGeneration != pageRequestGeneration || content.nextCursor != cursor) {
                return@update content
            }

            if (page.nextCursor == cursor) {
                content.copy(
                    nextCursor = null,
                    isLoadingMore = false,
                    loadMoreError = AppError.InvalidCursor,
                )
            } else {
                content.copy(
                    trips = (content.trips + page.items.map(SavedTrip::toUiModel))
                        .distinctBy(SavedTripUiModel::savedTripId),
                    nextCursor = page.nextCursor,
                    isLoadingMore = false,
                    loadMoreError = null,
                )
            }
        }
    }

    private fun setLoadMoreError(
        cursor: String,
        requestGeneration: Long,
        error: AppError,
    ) {
        _uiState.update { state ->
            val content = state as? SavedTripsUiState.Content ?: return@update state
            if (requestGeneration != pageRequestGeneration || content.nextCursor != cursor) {
                return@update content
            }

            content.copy(
                isLoadingMore = false,
                loadMoreError = error,
            )
        }
    }

    private fun observeBookmarkChanges() {
        viewModelScope.launch {
            bookmarkChangeNotifier.changes.collect(::handleBookmarkChange)
        }
    }

    private fun handleBookmarkChange(change: BookmarkChange) {
        if (!change.isSaved && removeSavedTripsFromState { trip -> trip.courseId == change.courseId }) {
            refreshAfterRemoval()
        }
    }

    private fun removeSavedTripsFromState(predicate: (SavedTripUiModel) -> Boolean): Boolean {
        val content = _uiState.value as? SavedTripsUiState.Content ?: return false
        val removedSavedTripIds = content.trips.filter(predicate).map(SavedTripUiModel::savedTripId).toSet()
        if (removedSavedTripIds.isEmpty()) return false

        val updatedTrips = content.trips.filterNot(predicate)
        _uiState.value = if (updatedTrips.isEmpty()) {
            SavedTripsUiState.Empty
        } else {
            content.copy(
                trips = updatedTrips,
                isLoadingMore = false,
                loadMoreError = null,
                removingSavedTripIds = content.removingSavedTripIds - removedSavedTripIds,
            )
        }
        return true
    }

    private fun refreshAfterRemoval() {
        val requestGeneration = ++pageRequestGeneration
        viewModelScope.launch {
            when (val result = getSavedTrips()) {
                is AppResult.Success -> {
                    if (requestGeneration == pageRequestGeneration) {
                        _uiState.value = result.value.toUiState()
                    }
                }

                is AppResult.Failure -> Unit
            }
        }
    }

    private fun clearRemovingState(savedTripId: String) {
        _uiState.update { state ->
            val content = state as? SavedTripsUiState.Content ?: return@update state
            content.copy(
                removingSavedTripIds = content.removingSavedTripIds - savedTripId,
            )
        }
    }
}

private fun SavedTripPage.toUiState(): SavedTripsUiState =
    if (items.isEmpty()) {
        SavedTripsUiState.Empty
    } else {
        SavedTripsUiState.Content(
            trips = items.map(SavedTrip::toUiModel),
            nextCursor = nextCursor,
        )
    }

private fun SavedTrip.toUiModel(): SavedTripUiModel = SavedTripUiModel(
    savedTripId = savedTripId,
    courseId = courseId,
    regionName = regionName,
    reason = reason,
    spotNames = spotNames
        .filter(String::isNotBlank)
        .ifEmpty { listOfNotNull(regionArea?.takeIf(String::isNotBlank)) },
    spotImageUrls = spotImageUrls
        .filter(String::isNotBlank)
        .ifEmpty { listOfNotNull(imageUrl?.takeIf(String::isNotBlank)) },
)

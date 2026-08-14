package com.example.sairo14.feature.onboarding.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.repository.OnboardingAnalysisSessionStore
import com.example.sairo14.domain.usecase.CreateOnboardingCompletionRequestUseCase
import com.example.sairo14.domain.usecase.DeleteSavedTripUseCase
import com.example.sairo14.domain.usecase.SaveTripUseCase
import com.example.sairo14.domain.usecase.UpdateOnboardingCompletionUseCase
import com.example.sairo14.feature.bookmark.BookmarkEffect
import com.example.sairo14.feature.bookmark.BookmarkUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 온보딩 추천 결과를 조회하고 카드의 화면 상태를 관리한다.
 *
 * 로딩 화면이 저장한 분석 결과를 읽고 결과 수에 따라 온보딩 완료 상태를 저장하거나 해제해
 * [OnboardingResultUiState]로 노출한다. 이전 세션 조회가 늦게 끝나도 최신 세션의 화면·완료 상태를
 * 덮어쓰지 않는다. 코스별 북마크의 서버 저장 결과와 요청 상태는 화면 수명 동안 관리하고, 실패는
 * [BookmarkEffect]로 한 번만 전달한다.
 */
@HiltViewModel
class OnboardingResultViewModel @Inject constructor(
    private val sessionStore: OnboardingAnalysisSessionStore,
    private val createOnboardingCompletionRequest: CreateOnboardingCompletionRequestUseCase,
    private val updateOnboardingCompletion: UpdateOnboardingCompletionUseCase,
    private val saveTripUseCase: SaveTripUseCase,
    private val deleteSavedTripUseCase: DeleteSavedTripUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<OnboardingResultUiState>(OnboardingResultUiState.Loading)

    val uiState: StateFlow<OnboardingResultUiState> = _uiState.asStateFlow()

    private val _bookmarkEffect = MutableSharedFlow<BookmarkEffect>(extraBufferCapacity = 1)

    /** 북마크 요청 실패를 화면에 한 번만 전달한다. */
    val bookmarkEffect: SharedFlow<BookmarkEffect> = _bookmarkEffect.asSharedFlow()

    private var searchSessionId: String? = null
    private var loadJob: Job? = null
    private var loadGeneration = 0L

    /** 세션의 분석 결과를 한 번 읽고 결과 수에 맞게 온보딩 완료 상태를 갱신한다. */
    fun load(searchSessionId: String, force: Boolean = false) {
        if (!force && this.searchSessionId == searchSessionId) return

        loadJob?.cancel()
        val generation = ++loadGeneration
        this.searchSessionId = searchSessionId
        loadJob = viewModelScope.launch {
            _uiState.value = OnboardingResultUiState.Loading

            val completionToken = when (val tokenResult = createOnboardingCompletionRequest()) {
                is AppResult.Success -> tokenResult.value
                is AppResult.Failure -> {
                    if (isCurrentGeneration(generation)) {
                        _uiState.value = OnboardingResultUiState.Error
                    }
                    return@launch
                }
            }
            if (!isCurrentGeneration(generation)) return@launch

            val recommendations = sessionStore.getResult(searchSessionId)?.recommendations
            if (!isCurrentGeneration(generation)) return@launch
            if (recommendations == null) {
                _uiState.value = OnboardingResultUiState.Error
                return@launch
            }

            val completionResult = updateOnboardingCompletion(recommendations, completionToken)
            if (!isCurrentGeneration(generation)) return@launch

            _uiState.value = when (completionResult) {
                is AppResult.Success -> {
                    if (completionResult.value) {
                        OnboardingResultUiState.Content(
                            recommendations = recommendations,
                            bookmarks = recommendations.associate { recommendation ->
                                recommendation.courseId to BookmarkUiState(
                                    isSaved = recommendation.isSaved,
                                )
                            },
                        )
                    } else {
                        OnboardingResultUiState.Error
                    }
                }
                is AppResult.Failure -> OnboardingResultUiState.Error
            }
        }
    }

    /** 마지막 세션 조회에 실패했을 때 같은 세션 결과를 다시 읽는다. */
    fun retry() {
        searchSessionId?.let { sessionId -> load(sessionId, force = true) }
    }

    /** 추천 카드의 북마크를 서버에 저장하거나 저장 해제한다.
     *
     * 요청 중인 카드는 다시 요청하지 않는다. 저장 해제에는 [BookmarkUiState.savedTripId]가 필요하므로
     * ID가 없는 체크 상태는 그대로 유지한다.
     * @param courseId 저장 상태를 변경할 추천 코스의 안정적인 ID
     */
    fun onBookmarkClick(courseId: String) {
        val bookmark = currentBookmark(courseId) ?: return
        if (bookmark.isRequesting) return

        if (bookmark.isSaved) {
            bookmark.savedTripId?.let { savedTripId -> deleteSavedTrip(courseId, savedTripId) }
        } else {
            saveTrip(courseId)
        }
    }

    private fun saveTrip(courseId: String) {
        setRequesting(courseId, isRequesting = true)
        viewModelScope.launch {
            when (val result = saveTripUseCase(courseId)) {
                is AppResult.Success -> updateBookmark(courseId) {
                    it.copy(
                        isSaved = true,
                        savedTripId = result.value.savedTripId,
                        isRequesting = false,
                    )
                }

                is AppResult.Failure -> {
                    setRequesting(courseId, isRequesting = false)
                    _bookmarkEffect.tryEmit(BookmarkEffect.ShowError(result.error))
                }
            }
        }
    }

    private fun deleteSavedTrip(courseId: String, savedTripId: String) {
        setRequesting(courseId, isRequesting = true)
        viewModelScope.launch {
            when (val result = deleteSavedTripUseCase(savedTripId)) {
                is AppResult.Success -> updateBookmark(courseId) {
                    it.copy(
                        isSaved = false,
                        savedTripId = null,
                        isRequesting = false,
                    )
                }

                is AppResult.Failure -> {
                    setRequesting(courseId, isRequesting = false)
                    _bookmarkEffect.tryEmit(BookmarkEffect.ShowError(result.error))
                }
            }
        }
    }

    private fun currentBookmark(courseId: String): BookmarkUiState? =
        (_uiState.value as? OnboardingResultUiState.Content)?.bookmarks?.get(courseId)

    private fun setRequesting(courseId: String, isRequesting: Boolean) {
        updateBookmark(courseId) { bookmark -> bookmark.copy(isRequesting = isRequesting) }
    }

    private inline fun updateBookmark(
        courseId: String,
        transform: (BookmarkUiState) -> BookmarkUiState,
    ) {
        _uiState.update { state ->
            val content = state as? OnboardingResultUiState.Content ?: return@update state
            val bookmark = content.bookmarks[courseId] ?: return@update content
            content.copy(bookmarks = content.bookmarks + (courseId to transform(bookmark)))
        }
    }

    private fun isCurrentGeneration(generation: Long): Boolean = generation == loadGeneration
}

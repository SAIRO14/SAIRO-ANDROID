package com.example.sairo14.feature.onboarding.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.repository.OnboardingAnalysisSessionStore
import com.example.sairo14.domain.usecase.UpdateOnboardingCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 온보딩 추천 결과를 조회하고 카드의 화면 상태를 관리한다.
 *
 * 로딩 화면이 저장한 분석 결과를 읽고 결과 수에 따라 온보딩 완료 상태를 저장하거나 해제해
 * [OnboardingResultUiState]로 노출한다. 북마크는 저장 여행 기능이 연결되기 전까지 현재 화면의
 * 표시 상태만 변경한다.
 */
@HiltViewModel
class OnboardingResultViewModel @Inject constructor(
    private val sessionStore: OnboardingAnalysisSessionStore,
    private val updateOnboardingCompletion: UpdateOnboardingCompletionUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<OnboardingResultUiState>(OnboardingResultUiState.Loading)

    val uiState: StateFlow<OnboardingResultUiState> = _uiState.asStateFlow()

    private var searchSessionId: String? = null

    /** 세션의 분석 결과를 한 번 읽고 결과 수에 맞게 온보딩 완료 상태를 갱신한다. */
    fun load(searchSessionId: String, force: Boolean = false) {
        if (!force && this.searchSessionId == searchSessionId) return

        this.searchSessionId = searchSessionId
        viewModelScope.launch {
            _uiState.value = OnboardingResultUiState.Loading

            val recommendations = sessionStore.getResult(searchSessionId)?.recommendations
                ?: run {
                    _uiState.value = OnboardingResultUiState.Error
                    return@launch
                }
            _uiState.value = when (updateOnboardingCompletion(recommendations)) {
                is AppResult.Success -> OnboardingResultUiState.Content(recommendations)
                is AppResult.Failure -> OnboardingResultUiState.Error
            }
        }
    }

    /** 마지막 세션 조회에 실패했을 때 같은 세션 결과를 다시 읽는다. */
    fun retry() {
        searchSessionId?.let { sessionId -> load(sessionId, force = true) }
    }

    /** 현재 화면에서 추천 카드의 북마크 표시만 전환한다. */
    fun toggleSaved(recommendationId: String) {
        _uiState.update { state ->
            val content = state as? OnboardingResultUiState.Content ?: return@update state
            content.copy(
                recommendations = content.recommendations.map { recommendation ->
                    if (recommendation.id == recommendationId) {
                        recommendation.copy(isSaved = !recommendation.isSaved)
                    } else {
                        recommendation
                    }
                },
            )
        }
    }
}

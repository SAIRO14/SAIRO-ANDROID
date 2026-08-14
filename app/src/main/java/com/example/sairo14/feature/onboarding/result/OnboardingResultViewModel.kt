package com.example.sairo14.feature.onboarding.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.repository.OnboardingAnalysisSessionStore
import com.example.sairo14.domain.usecase.CreateOnboardingCompletionRequestUseCase
import com.example.sairo14.domain.usecase.UpdateOnboardingCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 온보딩 추천 결과를 조회하고 카드의 화면 상태를 관리한다.
 *
 * 로딩 화면이 저장한 분석 결과를 읽고 결과 수에 따라 온보딩 완료 상태를 저장하거나 해제해
 * [OnboardingResultUiState]로 노출한다. 이전 세션 조회가 늦게 끝나도 최신 세션의 화면·완료 상태를
 * 덮어쓰지 않으며, 북마크는 저장 여행 기능이 연결되기 전까지 현재 화면의 표시 상태만 변경한다.
 */
@HiltViewModel
class OnboardingResultViewModel @Inject constructor(
    private val sessionStore: OnboardingAnalysisSessionStore,
    private val createOnboardingCompletionRequest: CreateOnboardingCompletionRequestUseCase,
    private val updateOnboardingCompletion: UpdateOnboardingCompletionUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<OnboardingResultUiState>(OnboardingResultUiState.Loading)

    val uiState: StateFlow<OnboardingResultUiState> = _uiState.asStateFlow()

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
                        OnboardingResultUiState.Content(recommendations)
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

    private fun isCurrentGeneration(generation: Long): Boolean = generation == loadGeneration
}

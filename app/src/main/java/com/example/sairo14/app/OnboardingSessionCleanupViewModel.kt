package com.example.sairo14.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.usecase.ClearOnboardingAnalysisSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** 온보딩 탐색을 완전히 종료할 때 임시 분석 세션을 정리한다.
 *
 * 홈 이동과 재추천 시작 시 세션 삭제를 비동기로 요청하며, 화면 전환 자체는 내비게이션 계층이 담당한다.
 */
@HiltViewModel
class OnboardingSessionCleanupViewModel @Inject constructor(
    private val clearOnboardingAnalysisSession: ClearOnboardingAnalysisSessionUseCase,
) : ViewModel() {

    /** 더 이상 사용하지 않는 온보딩 분석 세션을 삭제한다.
     * @param searchSessionId 삭제할 온보딩 탐색 세션 ID
     */
    fun clear(searchSessionId: String) {
        viewModelScope.launch {
            clearOnboardingAnalysisSession(searchSessionId)
        }
    }
}

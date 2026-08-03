package com.example.sairo14.feature.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 홈 화면의 로딩·콘텐츠·오류 상태를 관리한다.
 *
 * 현재는 서버 계약이 정해지지 않아 저장 여행지가 없는 [HomeUiState.Content]를 노출한다.
 * 추후 Fake Repository와 실제 Repository를 연결하면 중앙 이미지와 저장 여행지 목록을 같은
 * UI 계약으로 제공하고, 실패 시 [HomeUiState.Error]와 재시도 결과를 갱신한다.
 */
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Content())

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** 오류 상태에서 기본 콘텐츠를 다시 표시한다. */
    fun retry() {
        if (_uiState.value is HomeUiState.Error) {
            _uiState.value = HomeUiState.Content()
        }
    }
}

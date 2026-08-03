package com.example.sairo14.feature.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 홈 화면의 중앙 탐색 이미지 상태를 관리한다.
 *
 * 현재는 서버 계약이 정해지지 않아 빈 상태를 노출하며, 추후 Fake Repository와 실제
 * Repository를 연결해 이미지와 저장 여행지 상태를 같은 UI 계약으로 제공한다.
 */
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}

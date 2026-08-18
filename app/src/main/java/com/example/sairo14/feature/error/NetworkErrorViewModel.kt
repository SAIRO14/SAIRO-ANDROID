package com.example.sairo14.feature.error

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.repository.NetworkStatus
import com.example.sairo14.domain.repository.NetworkStatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * 네트워크 오류 화면의 재시도 가능 상태를 시스템 연결 상태와 연결한다.
 *
 * 실패한 요청과 재시도 동작은 호출한 Feature ViewModel이 소유한다. 이 ViewModel은 연결 상태를 보조 정보로만
 * 사용해 오프라인 중 재시도 버튼을 비활성화하며, 실제 성공·실패 판정에는 관여하지 않는다.
 */
@HiltViewModel
class NetworkErrorViewModel @Inject constructor(
    networkStatusRepository: NetworkStatusRepository,
) : ViewModel() {

    /** 현재 시스템이 검증된 인터넷 연결을 보고해 재시도를 시도할 수 있는지 여부다. */
    val isRetryEnabled: StateFlow<Boolean> = networkStatusRepository.status
        .map { status -> status == NetworkStatus.Available }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true,
        )
}

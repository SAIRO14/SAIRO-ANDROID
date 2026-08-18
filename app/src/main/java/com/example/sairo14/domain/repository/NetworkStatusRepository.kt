package com.example.sairo14.domain.repository

import kotlinx.coroutines.flow.Flow

/** Android 구현에 의존하지 않고 현재 인터넷 연결 가능 상태를 관찰하는 계약이다. */
interface NetworkStatusRepository {

    /** 인터넷 연결을 검증할 수 있는지 여부를 연속적으로 제공한다. */
    val status: Flow<NetworkStatus>
}

/** 앱이 네트워크 요청 전후 안내에 사용할 수 있는 인터넷 연결 상태다. */
enum class NetworkStatus {
    Available,
    Unavailable,
}

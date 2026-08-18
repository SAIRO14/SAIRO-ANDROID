package com.example.sairo14.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.example.sairo14.domain.repository.NetworkStatus
import com.example.sairo14.domain.repository.NetworkStatusRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Android의 기본 네트워크 콜백을 앱에서 사용할 연결 상태 Flow로 변환한다.
 *
 * 이 상태는 실제 API 요청의 성공 여부를 보장하지 않으므로 요청을 차단하는 용도가 아니다. Repository의
 * 실제 요청 결과가 오류 판정의 기준이며, 이 Flow는 연결 복구 안내 같은 보조 UI에만 사용한다.
 */
@Singleton
class AndroidNetworkStatusRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : NetworkStatusRepository {

    override val status: Flow<NetworkStatus> = callbackFlow {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(connectivityManager.currentNetworkStatus())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                trySend(networkCapabilities.toNetworkStatus())
            }

            override fun onLost(network: Network) {
                trySend(connectivityManager.currentNetworkStatus())
            }
        }

        trySend(connectivityManager.currentNetworkStatus())
        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}

private fun ConnectivityManager.currentNetworkStatus(): NetworkStatus =
    activeNetwork
        ?.let(::getNetworkCapabilities)
        ?.toNetworkStatus()
        ?: NetworkStatus.Unavailable

private fun NetworkCapabilities.toNetworkStatus(): NetworkStatus =
    if (
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    ) {
        NetworkStatus.Available
    } else {
        NetworkStatus.Unavailable
    }

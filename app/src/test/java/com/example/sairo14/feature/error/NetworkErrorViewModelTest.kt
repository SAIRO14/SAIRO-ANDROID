package com.example.sairo14.feature.error

import com.example.sairo14.domain.repository.NetworkStatus
import com.example.sairo14.domain.repository.NetworkStatusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkErrorViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `시스템 연결 상태에 따라 재시도 가능 상태를 갱신한다`() = runTest(dispatcher) {
        val repository = FakeNetworkStatusRepository(NetworkStatus.Available)
        val viewModel = NetworkErrorViewModel(repository)
        advanceUntilIdle()

        assertTrue(viewModel.isRetryEnabled.value)

        repository.statusFlow.value = NetworkStatus.Unavailable
        advanceUntilIdle()

        assertFalse(viewModel.isRetryEnabled.value)

        repository.statusFlow.value = NetworkStatus.Available
        advanceUntilIdle()

        assertTrue(viewModel.isRetryEnabled.value)
    }

    private class FakeNetworkStatusRepository(
        initialStatus: NetworkStatus,
    ) : NetworkStatusRepository {
        val statusFlow = MutableStateFlow(initialStatus)

        override val status: Flow<NetworkStatus> = statusFlow
    }
}

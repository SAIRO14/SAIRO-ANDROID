package com.example.sairo14.data.remote

import com.example.sairo14.domain.model.AppError
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class NetworkCallTest {

    @Test
    fun `시간 초과는 재시도 가능한 시간 초과 오류로 변환한다`() {
        assertEquals(
            AppError.RequestTimeout,
            SocketTimeoutException().toAppError(),
        )
    }

    @Test
    fun `호스트를 찾지 못하면 네트워크 연결 불가 오류로 변환한다`() {
        assertEquals(
            AppError.NetworkUnavailable,
            UnknownHostException().toAppError(),
        )
    }

    @Test
    fun `응답 직렬화 실패는 잘못된 응답 오류로 변환한다`() {
        assertEquals(
            AppError.InvalidResponse,
            SerializationException().toAppError(),
        )
    }

    @Test
    fun `취소된 요청은 오류 결과로 바꾸지 않고 다시 던진다`() = runTest {
        try {
            networkCall<Unit> {
                throw CancellationException()
            }
            fail("CancellationException이 다시 던져져야 합니다.")
        } catch (_: CancellationException) {
        }
    }
}

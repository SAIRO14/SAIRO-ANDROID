package com.example.sairo14.data.remote

import com.example.sairo14.domain.model.AppError
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import retrofit2.HttpException
import retrofit2.Response

class RemoteErrorMapperTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `maps invalid cursor response to InvalidCursor`() {
        val error = httpException(
            statusCode = 400,
            body = """{"code":"INVALID_CURSOR","retryable":false,"traceId":"trace-1"}""",
        ).toRemoteAppError(json)

        assertEquals(AppError.InvalidCursor, error)
    }

    @Test
    fun `maps not found response to ResourceNotFound`() {
        val error = httpException(
            statusCode = 404,
            body = """{"code":"COURSE_NOT_FOUND","retryable":false}""",
        ).toRemoteAppError(json)

        assertEquals(AppError.ResourceNotFound, error)
    }

    @Test
    fun `maps server error with retry details`() {
        val error = httpException(
            statusCode = 500,
            body = """{"code":"INTERNAL_ERROR","retryable":false,"traceId":"trace-2"}""",
        ).toRemoteAppError(json)

        assertEquals(
            AppError.ServerFailure(retryable = false, traceId = "trace-2"),
            error,
        )
    }

    @Test
    fun `maps IO exception to NetworkUnavailable`() {
        assertEquals(
            AppError.NetworkUnavailable,
            IOException("offline").toRemoteAppError(json),
        )
    }

    @Test
    fun `maps socket timeout to NetworkUnavailable`() {
        assertEquals(
            AppError.NetworkUnavailable,
            SocketTimeoutException("timed out").toRemoteAppError(json),
        )
    }

    @Test
    fun `remote operation rethrows cancellation`() = runTest {
        val exception = try {
            runRemoteOperation(action = "사진을 읽지 못했습니다.", json = json) {
                throw CancellationException()
            }
            null
        } catch (cancelled: CancellationException) {
            cancelled
        }

        assertTrue(exception is CancellationException)
    }

    @Test
    fun `remote operation does not convert fatal JVM errors`() = runTest {
        val fatalError = AssertionError("fatal")

        val thrown = try {
            runRemoteOperation(action = "사진을 읽지 못했습니다.", json = json) {
                throw fatalError
            }
            null
        } catch (error: AssertionError) {
            error
        }

        assertSame(fatalError, thrown)
    }

    private fun httpException(
        statusCode: Int,
        body: String,
    ): HttpException = HttpException(
        Response.error<Any>(
            statusCode,
            body.toResponseBody("application/json".toMediaType()),
        ),
    )
}

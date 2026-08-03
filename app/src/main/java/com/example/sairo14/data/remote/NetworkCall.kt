package com.example.sairo14.data.remote

import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import timber.log.Timber

/**
 * Retrofit 호출의 기술 예외를 앱이 처리할 수 있는 [AppResult]로 변환한다.
 *
 * 화면이 사라져 취소된 요청은 오류 화면을 만들지 않도록 [CancellationException]을 다시 던진다.
 * 실제 Repository는 이 함수를 통해 API 호출을 감싸고, HTTP·네트워크 구현 세부 사항을 UI에 전달하지 않는다.
 * @param block 실행할 API 요청
 */
suspend inline fun <T> networkCall(
    crossinline block: suspend () -> T,
): AppResult<T> = try {
    AppResult.Success(block())
} catch (exception: CancellationException) {
    throw exception
} catch (exception: Throwable) {
    Timber.e(exception, "Network request failed")
    AppResult.Failure(exception.toAppError())
}

/** Retrofit·OkHttp 예외를 Domain 오류로 변환한다. */
fun Throwable.toAppError(): AppError = when (this) {
    is SocketTimeoutException -> AppError.RequestTimeout
    is UnknownHostException,
    is ConnectException,
    is IOException -> AppError.NetworkUnavailable
    is HttpException -> when (code()) {
        401,
        403,
        -> AppError.Unauthorized

        in 500..599 -> AppError.ServerUnavailable
        else -> AppError.Unknown
    }

    is SerializationException -> AppError.InvalidResponse
    else -> AppError.Unknown
}

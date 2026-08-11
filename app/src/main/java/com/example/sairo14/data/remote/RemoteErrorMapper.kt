package com.example.sairo14.data.remote

import com.example.sairo14.data.remote.dto.ErrorResponseDto
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import timber.log.Timber

/** 서버·네트워크 예외를 화면이 처리할 수 있는 [AppError]로 변환한다.
 *
 * HTTP 오류 본문은 [ErrorResponseDto]로만 읽고 서버의 원본 문구는 화면에 노출하지 않는다.
 * @param json 오류 본문을 읽는 데 사용하는 앱 공통 JSON 설정
 * @return UI와 Domain에서 처리할 수 있는 앱 오류
 */
fun Throwable.toRemoteAppError(json: Json): AppError = when (this) {
    is IOException -> AppError.NetworkUnavailable
    is HttpException -> toHttpAppError(json)
    else -> AppError.Unknown
}

/** 실제 서버 호출의 취소 처리와 공통 오류 변환을 수행한다.
 *
 * 코루틴 취소는 오류 결과로 바꾸지 않고 다시 던진다. 호출자는 서버 응답의 DTO를 Domain 모델로
 * 변환하는 작업만 [block] 안에 둔다.
 * @param action 로그에 남길 호출 목적 설명
 * @param json 오류 본문을 읽는 데 사용하는 앱 공통 JSON 설정
 * @param block 실행할 Retrofit 호출과 응답 변환 작업
 */
suspend fun <T> runRemoteOperation(
    action: String,
    json: Json,
    block: suspend () -> T,
): AppResult<T> = try {
    AppResult.Success(block())
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (throwable: Throwable) {
    val error = throwable.toRemoteAppError(json)
    when (error) {
        is AppError.ServerFailure -> Timber.e(
            throwable,
            "%s traceId=%s",
            action,
            error.traceId ?: "unavailable",
        )

        else -> Timber.e(throwable, action)
    }
    AppResult.Failure(error)
}

private fun HttpException.toHttpAppError(json: Json): AppError {
    val errorResponse = response()
        ?.errorBody()
        ?.string()
        ?.let { body -> json.decodeErrorResponseOrNull(body) }

    return when (code()) {
        HttpStatusBadRequest -> when (errorResponse?.code) {
            ErrorCodeInvalidCursor -> AppError.InvalidCursor
            else -> AppError.InvalidRequest
        }

        HttpStatusNotFound -> AppError.ResourceNotFound
        HttpStatusConflict -> AppError.Conflict
        in HttpStatusServerErrorRange -> AppError.ServerFailure(
            retryable = errorResponse?.retryable ?: true,
            traceId = errorResponse?.traceId,
        )

        else -> AppError.Unknown
    }
}

private fun Json.decodeErrorResponseOrNull(body: String): ErrorResponseDto? =
    runCatching { decodeFromString<ErrorResponseDto>(body) }.getOrNull()

private const val HttpStatusBadRequest = 400
private const val HttpStatusNotFound = 404
private const val HttpStatusConflict = 409
private val HttpStatusServerErrorRange = 500..599
private const val ErrorCodeInvalidCursor = "INVALID_CURSOR"

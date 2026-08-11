package com.example.sairo14.domain.model

/** 앱 계층 간 단발성 작업의 성공 또는 처리 가능한 실패를 표현한다. */
sealed interface AppResult<out T> {
    /** 작업이 성공적으로 완료된 상태다. */
    data class Success<T>(
        val value: T,
    ) : AppResult<T>

    /** 호출자가 사용자 흐름으로 처리할 수 있는 실패 상태다. */
    data class Failure(
        val error: AppError,
    ) : AppResult<Nothing>
}

/** 앱이 기술 구현을 알지 않고 처리할 수 있는 데이터 접근 오류를 정의한다. */
sealed interface AppError {
    /** 네트워크 연결 또는 응답 대기 문제로 서버에 접근할 수 없는 상태다. */
    data object NetworkUnavailable : AppError

    /** 서버가 요청 값 또는 현재 요청 조합을 허용하지 않은 상태다. */
    data object InvalidRequest : AppError

    /** 저장 목록의 다음 페이지 커서를 더 이상 사용할 수 없는 상태다. */
    data object InvalidCursor : AppError

    /** 요청한 코스나 여행지를 찾을 수 없는 상태다. */
    data object ResourceNotFound : AppError

    /** 동일한 요청의 현재 상태가 서버 데이터와 충돌한 상태다. */
    data object Conflict : AppError

    /** 서버가 처리하지 못한 요청의 재시도 가능 여부와 추적 정보를 표현한다.
     *
     * 서버 응답 원문은 Data 계층에 남기고, 화면은 이 상태로 재시도 가능 여부만 결정한다.
     * @param retryable 동일한 요청을 다시 보냈을 때 성공할 가능성이 있는지 여부
     * @param traceId 서버 로그와 대조할 수 있는 요청 추적 ID. 응답에 없으면 `null`
     */
    data class ServerFailure(
        val retryable: Boolean,
        val traceId: String? = null,
    ) : AppError

    /** 로컬 저장소에 일시적으로 접근할 수 없는 상태다. */
    data object StorageUnavailable : AppError

    /** 로컬 저장소 파일이 손상되어 정상적으로 읽을 수 없는 상태다. */
    data object StorageCorrupted : AppError

    /** 분류되지 않은 오류가 발생한 상태다. */
    data object Unknown : AppError
}

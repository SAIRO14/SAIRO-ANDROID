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
    /** 요청한 코스나 여행지를 찾을 수 없는 상태다. */
    data object ResourceNotFound : AppError

    /** 로컬 저장소 또는 네트워크에 일시적으로 접근할 수 없는 상태다. */
    data object StorageUnavailable : AppError

    /** 로컬 저장소 파일이 손상되어 정상적으로 읽을 수 없는 상태다. */
    data object StorageCorrupted : AppError

    /** 분류되지 않은 오류가 발생한 상태다. */
    data object Unknown : AppError
}

package com.example.sairo14.data.remote.dto

import com.example.sairo14.domain.model.AppError
import kotlinx.serialization.Serializable

/** SAIRO 서버가 HTTP 오류 본문으로 반환하는 공통 형식이다.
 *
 * 오류 본문은 중간 프록시나 예기치 않은 서버 상태에서 일부 필드가 빠질 수 있으므로, 모든 값은
 * nullable로 읽고 Data 계층의 오류 mapper가 안전한 [AppError]로 변환한다.
 * @param code 서버가 정의한 오류 식별자
 * @param message 서버 진단용 설명. 사용자 문구로 직접 표시하지 않는다
 * @param retryable 같은 요청을 다시 보냈을 때 성공할 가능성
 * @param traceId 서버 로그와 대조할 요청 추적 ID
 */
@Serializable
data class ErrorResponseDto(
    val code: String? = null,
    val message: String? = null,
    val retryable: Boolean? = null,
    val traceId: String? = null,
)

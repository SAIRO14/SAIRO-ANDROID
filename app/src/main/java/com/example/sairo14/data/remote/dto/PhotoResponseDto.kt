package com.example.sairo14.data.remote.dto

import kotlinx.serialization.Serializable

/** 사진 풀 조회 API가 반환하는 선택 후보 한 건이다.
 *
 * 서버 계약에서 두 값은 사진 카드를 구성하는 필수 값이다. 누락되거나 `null`인 응답은 역직렬화에
 * 실패하며 공통 네트워크 오류 처리 정책에 따라 실패로 변환된다.
 * @param id 사진을 식별하고 취향 분석 요청에 전달할 ID
 * @param imageUrl 사진 카드에 표시할 이미지 주소
 */
@Serializable
data class PhotoResponseDto(
    val id: String,
    val imageUrl: String,
)

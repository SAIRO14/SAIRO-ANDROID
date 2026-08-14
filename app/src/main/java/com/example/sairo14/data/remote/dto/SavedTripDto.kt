package com.example.sairo14.data.remote.dto

import kotlinx.serialization.Serializable

/** 저장 여행지 생성 API에 전달하는 코스 식별자다. */
@Serializable
data class SavedTripSaveRequestDto(
    val courseId: String,
)

/** 저장 여행지 생성 API가 반환하는 저장 항목 식별자다. */
@Serializable
data class SavedTripSaveResponseDto(
    val savedTripId: String,
    val courseId: String,
)

/** 저장 여행지 목록 API가 반환하는 페이지 응답이다. */
@Serializable
data class SavedTripListResponseDto(
    val items: List<SavedTripResponseDto>,
    val nextCursor: String? = null,
)

/** 저장 여행지 목록 카드에 필요한 서버 제공 요약 정보다. */
@Serializable
data class SavedTripResponseDto(
    val savedTripId: String,
    val courseId: String,
    val regionName: String,
    val regionArea: String? = null,
    val imageUrl: String? = null,
    val reason: String? = null,
    val createdAt: String,
)

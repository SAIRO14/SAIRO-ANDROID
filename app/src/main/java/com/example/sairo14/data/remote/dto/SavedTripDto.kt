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

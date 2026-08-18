package com.example.sairo14.data.remote.dto

import kotlinx.serialization.Serializable

/** 코스 공유 스냅샷 생성 API가 반환하는 식별자와 공개 URL이다. */
@Serializable
data class ShareCourseResponseDto(
    val shareId: String,
    val shareUrl: String,
)

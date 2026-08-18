package com.example.sairo14.domain.model

/** 읽기 전용 코스 공유 스냅샷의 식별자와 외부 공개 URL이다. */
data class SharedCourseLink(
    val shareId: String,
    val shareUrl: String,
)

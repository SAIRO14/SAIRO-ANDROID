package com.example.sairo14.domain.model

/** 여행지를 저장한 뒤 북마크 상태 갱신에 필요한 식별자를 표현한다. */
data class SavedTripSaveResult(
    val savedTripId: String,
    val courseId: String,
)

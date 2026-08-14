package com.example.sairo14.data.mapper

import com.example.sairo14.data.remote.dto.SavedTripSaveResponseDto
import com.example.sairo14.domain.model.SavedTripSaveResult

/** 저장 여행지 API 응답을 북마크 상태 변경에 필요한 Domain 모델로 변환한다. */
fun SavedTripSaveResponseDto.toDomain(): SavedTripSaveResult = SavedTripSaveResult(
    savedTripId = savedTripId,
    courseId = courseId,
)

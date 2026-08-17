package com.example.sairo14.data.mapper

import com.example.sairo14.data.remote.dto.SavedTripSaveResponseDto
import com.example.sairo14.data.remote.dto.SavedTripListResponseDto
import com.example.sairo14.data.remote.dto.SavedTripResponseDto
import com.example.sairo14.domain.model.SavedTrip
import com.example.sairo14.domain.model.SavedTripPage
import com.example.sairo14.domain.model.SavedTripSaveResult

/** 저장 여행지 API 응답을 북마크 상태 변경에 필요한 Domain 모델로 변환한다. */
fun SavedTripSaveResponseDto.toDomain(): SavedTripSaveResult = SavedTripSaveResult(
    savedTripId = savedTripId,
    courseId = courseId,
)

/** 저장 여행지 목록 응답을 페이지 단위 Domain 모델로 변환한다. */
fun SavedTripListResponseDto.toDomain(): SavedTripPage = SavedTripPage(
    items = items.map(SavedTripResponseDto::toDomain),
    nextCursor = nextCursor,
)

/** 저장 여행지 요약 응답의 선택 값을 보존한 채 Domain 모델로 변환한다. */
fun SavedTripResponseDto.toDomain(): SavedTrip = SavedTrip(
    savedTripId = savedTripId,
    courseId = courseId,
    regionName = regionName,
    regionArea = regionArea,
    imageUrl = imageUrl,
    reason = reason,
    spotNames = spotNames,
    spotImageUrls = spotImageUrls,
    createdAt = createdAt,
)

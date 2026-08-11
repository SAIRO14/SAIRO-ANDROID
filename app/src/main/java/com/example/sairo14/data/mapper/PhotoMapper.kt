package com.example.sairo14.data.mapper

import com.example.sairo14.data.remote.dto.PhotoResponseDto
import com.example.sairo14.domain.model.PhotoCandidate

/** 사진 API 응답을 온보딩 화면에서 사용할 도메인 모델로 변환한다. */
fun PhotoResponseDto.toDomain(): PhotoCandidate = PhotoCandidate(
    id = id,
    imageUrl = imageUrl,
    contentDescription = null,
)

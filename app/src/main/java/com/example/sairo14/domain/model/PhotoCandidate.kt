package com.example.sairo14.domain.model

/** 온보딩에서 사용자가 취향을 선택할 수 있는 사진 후보를 표현한다. */
data class PhotoCandidate(
    val id: String,
    val imageUrl: String,
    val contentDescription: String?,
)

package com.example.sairo14.domain.repository

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.PhotoCandidate

/** 온보딩 사진 선택 화면에 표시할 사진 후보를 조회하는 도메인 계약이다. */
interface PhotoSelectionRepository {

    /** 사용자가 취향을 선택할 수 있는 사진 후보를 한 번 조회한다. */
    suspend fun getPhotoCandidates(): AppResult<List<PhotoCandidate>>
}

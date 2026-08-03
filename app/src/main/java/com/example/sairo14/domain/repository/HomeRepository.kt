package com.example.sairo14.domain.repository

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.HomeContent

/** 홈 화면에 필요한 탐색 콘텐츠와 저장 여행지를 조회하는 도메인 계약이다. */
interface HomeRepository {

    /** 현재 사용자의 홈 콘텐츠를 한 번 조회한다. */
    suspend fun getHomeContent(): AppResult<HomeContent>
}

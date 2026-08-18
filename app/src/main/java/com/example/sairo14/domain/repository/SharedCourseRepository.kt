package com.example.sairo14.domain.repository

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SharedCourse

/** 외부 공유 링크의 읽기 전용 코스 스냅샷을 조회하는 도메인 계약이다. */
interface SharedCourseRepository {

    /** 지정한 공유 스냅샷을 현재 기기 식별자 없이 한 번 조회한다.
     *
     * @param shareId 공유 URL에서 파싱한 스냅샷 식별자
     */
    suspend fun getSharedCourse(shareId: String): AppResult<SharedCourse>
}

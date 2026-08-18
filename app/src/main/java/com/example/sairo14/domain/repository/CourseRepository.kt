package com.example.sairo14.domain.repository

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.Course

/** 여행 상세 화면에 필요한 코스 스냅샷을 조회하는 도메인 계약이다. */
interface CourseRepository {

    /** 지정한 코스 ID의 지역·일차·장소 정보를 한 번 조회한다.
     *
     * @param courseId 조회할 코스의 안정적인 ID
     */
    suspend fun getCourse(courseId: String): AppResult<Course>
}

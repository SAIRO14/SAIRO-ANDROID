package com.example.sairo14.data.mapper

import com.example.sairo14.data.remote.dto.SharedCourseResponseDto
import com.example.sairo14.domain.model.CourseDay
import com.example.sairo14.domain.model.SharedCourse

/** 공개 공유 코스 응답을 기기 상태를 포함하지 않는 [SharedCourse]로 변환한다. */
fun SharedCourseResponseDto.toDomain(shareId: String): SharedCourse = SharedCourse(
    shareId = shareId,
    courseId = courseId,
    regionName = regionName,
    days = listOf(
        CourseDay(dayNumber = 1, places = day1.map { it.toCoursePlace() }),
        CourseDay(dayNumber = 2, places = day2.map { it.toCoursePlace() }),
    ),
)

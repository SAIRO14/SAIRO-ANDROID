package com.example.sairo14.data.mapper

import com.example.sairo14.data.remote.dto.SharedCourseResponseDto
import com.example.sairo14.data.remote.dto.SpotSummaryDto
import com.example.sairo14.domain.model.MapCoordinate
import org.junit.Assert.assertEquals
import org.junit.Test

class SharedCourseMapperTest {

    @Test
    fun `공개 응답을 공유 ID와 일차 순서를 포함한 읽기 전용 코스로 변환한다`() {
        val result = SharedCourseResponseDto(
            courseId = "course-1",
            regionName = "제주도",
            day1 = listOf(
                SpotSummaryDto(
                    spotId = "spot-1",
                    name = "성산일출봉",
                    lat = 33.0,
                    lng = 126.0,
                    imageUrl = " https://example.com/spot.jpg ",
                    operatingHours = "09:00~18:00",
                ),
            ),
            day2 = emptyList(),
        ).toDomain(shareId = "share-1")

        assertEquals("share-1", result.shareId)
        assertEquals("course-1", result.courseId)
        assertEquals("제주도", result.regionName)
        assertEquals(listOf(1, 2), result.days.map { it.dayNumber })
        assertEquals("https://example.com/spot.jpg", result.days.first().places.single().imageUrl)
        assertEquals(MapCoordinate(33.0, 126.0), result.days.first().places.single().coordinate)
    }
}

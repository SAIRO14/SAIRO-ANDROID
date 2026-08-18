package com.example.sairo14.data.mapper

import com.example.sairo14.data.remote.dto.CourseResponseDto
import com.example.sairo14.data.remote.dto.SpotSummaryDto
import com.example.sairo14.domain.model.MapCoordinate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CourseMapperTest {

    @Test
    fun `maps day order saved state and structured place information`() {
        val result = response(
            saved = true,
            day1 = listOf(
                SpotSummaryDto(
                    spotId = "spot-1",
                    name = " 첫 장소 ",
                    lat = 33.0,
                    lng = 126.0,
                    imageUrl = " https://example.com/spot-1.jpg ",
                    operatingHours = " 09:00~18:00 ",
                    closedDays = " 월요일 ",
                    parking = " 가능 ",
                    contact = " 000-0000-0000 ",
                ),
            ),
            day2 = listOf(
                SpotSummaryDto(
                    spotId = "spot-2",
                    name = "둘째 날 장소",
                    parking = "불가능",
                ),
            ),
        ).toDomain()

        assertEquals("course-1", result.courseId)
        assertEquals("제주도", result.regionName)
        assertEquals(true, result.isSaved)
        assertEquals(listOf(1, 2), result.days.map { day -> day.dayNumber })

        val firstPlace = result.days[0].places.single()
        assertEquals(" 첫 장소 ", firstPlace.name)
        assertEquals("https://example.com/spot-1.jpg", firstPlace.imageUrl)
        assertEquals(MapCoordinate(33.0, 126.0), firstPlace.coordinate)
        assertEquals(
            listOf("09:00~18:00", "월요일", "가능", "000-0000-0000"),
            firstPlace.tags,
        )
        assertEquals("가능", firstPlace.parking)

        assertEquals("spot-2", result.days[1].places.single().placeId)
        assertEquals(listOf("불가능"), result.days[1].places.single().tags)
    }

    @Test
    fun `keeps place and omits coordinate when either coordinate is unavailable`() {
        val result = response(
            day1 = listOf(
                SpotSummaryDto(
                    spotId = "spot-1",
                    name = "좌표 없는 장소",
                    lat = 33.0,
                    imageUrl = " ",
                    operatingHours = " ",
                    closedDays = " ",
                ),
            ),
        ).toDomain()

        val place = result.days.first().places.single()
        assertNull(place.coordinate)
        assertNull(place.imageUrl)
        assertEquals(emptyList<String>(), place.tags)
        assertNull(place.operatingHours)
        assertNull(place.closedDays)
    }

    private fun response(
        saved: Boolean = false,
        day1: List<SpotSummaryDto> = emptyList(),
        day2: List<SpotSummaryDto> = emptyList(),
    ) = CourseResponseDto(
        courseId = "course-1",
        regionName = "제주도",
        regionArea = "제주특별자치도",
        imageUrl = "https://example.com/course.jpg",
        reason = "바다와 어울리는 코스",
        saved = saved,
        day1 = day1,
        day2 = day2,
    )
}

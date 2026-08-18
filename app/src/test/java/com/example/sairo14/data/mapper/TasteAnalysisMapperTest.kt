package com.example.sairo14.data.mapper

import com.example.sairo14.data.remote.dto.CourseCardDto
import com.example.sairo14.data.remote.dto.SpotSummaryDto
import com.example.sairo14.data.remote.dto.TasteAnalysisResponseDto
import com.example.sairo14.domain.model.MapCoordinate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TasteAnalysisMapperTest {

    @Test
    fun `maps analysis tags cards and course details`() {
        val result = response(
            day1 = listOf(
                SpotSummaryDto(
                    spotId = "spot-1",
                    name = " 첫 장소 ",
                    lat = 33.0,
                    lng = 126.0,
                    imageUrl = " https://example.com/spot-1.jpg ",
                    operatingHours = "09:00~18:00",
                    closedDays = "월요일",
                    parking = "가능",
                    contact = "000-0000-0000",
                ),
                SpotSummaryDto(
                    spotId = "spot-2",
                    name = "첫 장소",
                    imageUrl = "https://example.com/spot-1.jpg",
                ),
            ),
        ).toDomain()

        assertEquals(listOf("고요한", "따뜻한"), result.moodTags)
        assertEquals("제주도", result.recommendations.single().regionName)
        assertEquals("분석 요약", result.recommendations.single().description)
        assertEquals(
            listOf("https://example.com/course.jpg", "https://example.com/spot-1.jpg"),
            result.recommendations.single().imageUrls,
        )
        assertEquals(listOf("첫 장소"), result.recommendations.single().placeNames)

        val place = result.courses.getValue("course-1").days.first().places.first()
        assertEquals(MapCoordinate(33.0, 126.0), place.coordinate)
        assertEquals("09:00~18:00", place.operatingHours)
        assertEquals(listOf("09:00~18:00", "월요일", "가능", "000-0000-0000"), place.tags)
        assertEquals("가능", place.parking)
    }

    @Test
    fun `keeps unavailable parking value in domain model`() {
        val result = response(
            day1 = listOf(
                SpotSummaryDto(
                    spotId = "spot-1",
                    name = "주차 불가 장소",
                    operatingHours = "10:00~20:00",
                    parking = " 불가능 ",
                    contact = "010-0000-0000",
                ),
            ),
        ).toDomain()

        val place = result.courses.getValue("course-1").days.first().places.single()
        assertEquals(listOf("10:00~20:00", "불가능", "010-0000-0000"), place.tags)
        assertEquals("불가능", place.parking)
    }

    @Test
    fun `normalizes place info HTML breaks raw line breaks and line whitespace`() {
        val result = response(
            day1 = listOf(
                SpotSummaryDto(
                    spotId = "spot-1",
                    name = "줄바꿈 장소",
                    operatingHours = " [하절기]<br> \r\n 09:00~18:00 <br/>\n [동절기] <br />  \n09:00~17:00 ",
                    closedDays = "  매주 월요일  \r\n\r\n 설·추석 연휴 ",
                    parking = " 가능 <br> \n 요금 (무료) ",
                    contact = " 관광안내소 \n 064-740-6000 ",
                ),
            ),
        ).toDomain()

        val place = result.courses.getValue("course-1").days.first().places.single()
        assertEquals("[하절기]\n09:00~18:00\n[동절기]\n09:00~17:00", place.operatingHours)
        assertEquals("매주 월요일\n설·추석 연휴", place.closedDays)
        assertEquals("가능\n요금 (무료)", place.parking)
        assertEquals("관광안내소\n064-740-6000", place.contact)
        assertEquals(
            listOf(
                "[하절기]\n09:00~18:00\n[동절기]\n09:00~17:00",
                "매주 월요일\n설·추석 연휴",
                "가능\n요금 (무료)",
                "관광안내소\n064-740-6000",
            ),
            place.tags,
        )
    }

    @Test
    fun `maps place info containing only whitespace and breaks to null`() {
        val result = response(
            day1 = listOf(
                SpotSummaryDto(
                    spotId = "spot-1",
                    name = "정보 없는 장소",
                    operatingHours = " <br> \n <br/> ",
                    closedDays = " \r\n ",
                ),
            ),
        ).toDomain()

        val place = result.courses.getValue("course-1").days.first().places.single()
        assertNull(place.operatingHours)
        assertNull(place.closedDays)
        assertEquals(emptyList<String>(), place.tags)
    }

    @Test
    fun `uses summary and keeps place when only one coordinate is missing`() {
        val result = response(
            reason = " ",
            day1 = listOf(
                SpotSummaryDto(
                    spotId = "spot-1",
                    name = "좌표 없는 장소",
                    lat = 33.0,
                ),
            ),
        ).toDomain()

        assertEquals("분석 요약", result.recommendations.single().description)
        assertNull(result.courses.getValue("course-1").days.first().places.single().coordinate)
    }

    @Test
    fun `preserves saved state in both recommendation and course detail models`() {
        val result = response(saved = true).toDomain()

        assertEquals(true, result.recommendations.single().isSaved)
        assertEquals(true, result.courses.getValue("course-1").isSaved)
    }

    private fun response(
        reason: String? = null,
        day1: List<SpotSummaryDto> = emptyList(),
        saved: Boolean = false,
    ): TasteAnalysisResponseDto = TasteAnalysisResponseDto(
        moodTags = listOf(" 고요한 ", "따뜻한", "고요한", " "),
        summary = "분석 요약",
        courses = listOf(
            CourseCardDto(
                courseId = "course-1",
                regionName = "제주도",
                regionArea = "제주특별자치도",
                imageUrl = " https://example.com/course.jpg ",
                reason = reason,
                saved = saved,
                day1 = day1,
                day2 = emptyList(),
            ),
        ),
    )
}

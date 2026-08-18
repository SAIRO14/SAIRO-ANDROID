package com.example.sairo14.data.mapper

import com.example.sairo14.data.remote.dto.CourseCardDto
import com.example.sairo14.data.remote.dto.CourseResponseDto
import com.example.sairo14.data.remote.dto.SpotSummaryDto
import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.model.CourseDay
import com.example.sairo14.domain.model.CoursePlace
import com.example.sairo14.domain.model.MapCoordinate

/** 코스 상세 조회 응답을 여행 상세 화면에 필요한 [Course]로 변환한다.
 *
 * 지역의 대표 이미지와 추천 이유처럼 현재 상세 화면에서 사용하지 않는 값은 DTO 경계에 남기고 Domain에는
 * 전달하지 않는다.
 */
fun CourseResponseDto.toDomain(): Course = toCourse(
    courseId = courseId,
    regionName = regionName,
    saved = saved,
    day1 = day1,
    day2 = day2,
)

internal fun CourseCardDto.toCourse(): Course = toCourse(
    courseId = courseId,
    regionName = regionName,
    saved = saved,
    day1 = day1,
    day2 = day2,
)

internal fun SpotSummaryDto.toCoursePlace(): CoursePlace {
    val normalizedOperatingHours = operatingHours.normalizePlaceInfoText()
    val normalizedClosedDays = closedDays.normalizePlaceInfoText()
    val normalizedParking = parking.normalizePlaceInfoText()
    val normalizedContact = contact.normalizePlaceInfoText()

    return CoursePlace(
        placeId = spotId,
        name = name,
        imageUrl = imageUrl.trimToNull(),
        tags = listOfNotNull(
            normalizedOperatingHours,
            normalizedClosedDays,
            normalizedParking,
            normalizedContact,
        ).distinct(),
        coordinate = lat?.let { latitude ->
            lng?.let { longitude -> MapCoordinate(latitude = latitude, longitude = longitude) }
        },
        operatingHours = normalizedOperatingHours,
        closedDays = normalizedClosedDays,
        parking = normalizedParking,
        contact = normalizedContact,
    )
}

private fun toCourse(
    courseId: String,
    regionName: String,
    saved: Boolean,
    day1: List<SpotSummaryDto>,
    day2: List<SpotSummaryDto>,
): Course = Course(
    courseId = courseId,
    regionName = regionName,
    days = listOf(
        CourseDay(dayNumber = 1, places = day1.map(SpotSummaryDto::toCoursePlace)),
        CourseDay(dayNumber = 2, places = day2.map(SpotSummaryDto::toCoursePlace)),
    ),
    isSaved = saved,
)

internal fun String?.trimToNull(): String? = this?.trim()?.takeIf(String::isNotEmpty)

private fun String?.normalizePlaceInfoText(): String? = this
    ?.replace(Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE), "\n")
    ?.replace("\r\n", "\n")
    ?.replace('\r', '\n')
    ?.lineSequence()
    ?.map(String::trim)
    ?.filter(String::isNotEmpty)
    ?.joinToString("\n")
    ?.takeIf(String::isNotEmpty)

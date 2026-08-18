package com.example.sairo14.data.mapper

import com.example.sairo14.data.remote.dto.CourseCardDto
import com.example.sairo14.data.remote.dto.SpotSummaryDto
import com.example.sairo14.data.remote.dto.TasteAnalysisResponseDto
import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.model.OnboardingRecommendation

/** 취향 분석 API 응답을 로딩·추천 결과·지도 상세 화면에서 공유할 Domain 모델로 변환한다. */
fun TasteAnalysisResponseDto.toDomain(): OnboardingAnalysisResult {
    val courses = courses.map(CourseCardDto::toCourse)

    return OnboardingAnalysisResult(
        moodTags = moodTags.normalizedValues(),
        summary = summary,
        recommendations = this.courses.map { course ->
            course.toOnboardingRecommendation(summary = summary)
        },
        courses = courses.associateBy(Course::courseId),
    )
}

private fun CourseCardDto.toOnboardingRecommendation(summary: String): OnboardingRecommendation =
    OnboardingRecommendation(
        id = courseId,
        courseId = courseId,
        regionName = regionName,
        description = reason.trimToNull() ?: summary,
        imageUrls = buildList {
            imageUrl.trimToNull()?.let(::add)
            (day1 + day2).forEach { spot ->
                spot.imageUrl.trimToNull()?.let(::add)
            }
        }.normalizedValues(),
        placeNames = (day1 + day2).map(SpotSummaryDto::name).normalizedValues(),
        isSaved = saved,
    )

private fun List<String>.normalizedValues(): List<String> =
    asSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
        .toList()

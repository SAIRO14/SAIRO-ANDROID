package com.example.sairo14.data.repository.fake

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.model.CourseDay
import com.example.sairo14.domain.model.CoursePlace
import com.example.sairo14.domain.model.MapCoordinate
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.model.OnboardingRecommendation
import com.example.sairo14.domain.repository.OnboardingRecommendationRepository
import javax.inject.Inject
import javax.inject.Singleton

/** 실제 API 없이 온보딩 분석 결과·무드 태그·코스 상세 흐름을 확인할 수 있도록 고정 결과를 제공한다. */
@Singleton
class FakeOnboardingRecommendationRepository @Inject constructor() : OnboardingRecommendationRepository {

    override suspend fun analyzeTaste(
        selectedPhotoIds: List<String>,
    ): AppResult<OnboardingAnalysisResult> = AppResult.Success(sampleAnalysisResult)

    private companion object {
        val sampleRecommendations = listOf(
            recommendation(
                courseId = "course-boeun",
                regionName = "충북 보은권",
                description = "고요한 자연과 전통의 분위기",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=900&q=85",
                    "https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=900&q=85",
                ),
                placeNames = listOf("말티재 전망대", "세조길 숲 산책"),
            ),
            recommendation(
                courseId = "course-gangneung",
                regionName = "강원 강릉권",
                description = "바다와 골목이 어우러진 느긋한 풍경",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=900&q=85",
                    "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=900&q=85",
                ),
                placeNames = listOf("안목해변", "명주동 골목"),
            ),
            recommendation(
                courseId = "course-jeju",
                regionName = "제주 서부권",
                description = "빛과 바람을 따라 걷는 한적한 하루",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1439853949127-fa647821eba0?auto=format&fit=crop&w=900&q=85",
                    "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=900&q=85",
                ),
                placeNames = listOf("협재해수욕장", "금능해안"),
            ),
        )

        val sampleAnalysisResult = OnboardingAnalysisResult(
            moodTags = listOf("고요한", "따뜻한", "한적한"),
            summary = "자연 속에서 여유를 즐기는 취향이에요.",
            recommendations = sampleRecommendations,
            courses = sampleRecommendations
                .map { recommendation -> recommendation.toCourse() }
                .associateBy(Course::courseId),
        )

        private fun recommendation(
            courseId: String,
            regionName: String,
            description: String,
            imageUrls: List<String>,
            placeNames: List<String>,
        ): OnboardingRecommendation = OnboardingRecommendation(
            id = "recommendation-$courseId",
            courseId = courseId,
            regionName = regionName,
            description = description,
            imageUrls = imageUrls,
            placeNames = placeNames,
        )

        private fun OnboardingRecommendation.toCourse(): Course = Course(
            courseId = courseId,
            regionName = regionName,
            days = listOf(
                CourseDay(
                    dayNumber = 1,
                    places = placeNames.mapIndexed { index, name ->
                        CoursePlace(
                            placeId = "$courseId-place-$index",
                            name = name,
                            imageUrl = imageUrls.getOrNull(index),
                            tags = emptyList(),
                            coordinate = MapCoordinate(
                                latitude = 36.0 + index,
                                longitude = 127.0 + index,
                            ),
                        )
                    },
                ),
                CourseDay(dayNumber = 2, places = emptyList()),
            ),
        )
    }
}

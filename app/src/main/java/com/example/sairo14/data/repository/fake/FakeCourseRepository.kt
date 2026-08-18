package com.example.sairo14.data.repository.fake

import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.model.CourseDay
import com.example.sairo14.domain.model.CoursePlace
import com.example.sairo14.domain.model.MapCoordinate
import com.example.sairo14.domain.model.SharedCourseLink
import com.example.sairo14.domain.repository.CourseRepository
import javax.inject.Inject
import javax.inject.Singleton

/** 서버 연동 전 일차별 상세 화면과 지도 핀을 확인할 수 있도록 고정 코스 데이터를 제공한다. */
@Singleton
class FakeCourseRepository @Inject constructor() : CourseRepository {

    override suspend fun getCourse(courseId: String): AppResult<Course> =
        sampleCourses[courseId]
            ?.let { course -> AppResult.Success(course) }
            ?: AppResult.Failure(AppError.ResourceNotFound)

    override suspend fun createShareLink(courseId: String): AppResult<SharedCourseLink> =
        sampleCourses[courseId]
            ?.let {
                AppResult.Success(
                    SharedCourseLink(
                        shareId = "fake-share-$courseId",
                        shareUrl = "https://example.com/shared/$courseId",
                    ),
                )
            }
            ?: AppResult.Failure(AppError.ResourceNotFound)

    private companion object {
        val sampleCourses = listOf(
            Course(
                courseId = "course-boeun",
                regionName = "충북 보은권",
                days = listOf(
                    CourseDay(
                        dayNumber = 1,
                        places = listOf(
                            CoursePlace(
                                placeId = "boeun-maltijae-observatory",
                                name = "말티재 전망대",
                                imageUrl = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=900&q=85",
                                tags = listOf("09:00~18:00", "월 휴무", "주차가능"),
                                coordinate = MapCoordinate(36.4894, 127.7469),
                            ),
                            CoursePlace(
                                placeId = "boeun-sejogil-forest-walk",
                                name = "세조길 숲 산책",
                                imageUrl = "https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=900&q=85",
                                tags = listOf("09:00~18:00", "월 휴무", "주차가능"),
                                coordinate = MapCoordinate(36.5452, 127.8338),
                            ),
                            CoursePlace(
                                placeId = "boeun-samnyeonsanseong",
                                name = "보은 삼년산성",
                                imageUrl = "https://images.unsplash.com/photo-1470770841072-f978cf4d019e?auto=format&fit=crop&w=900&q=85",
                                tags = listOf("상시 개방", "주차가능"),
                                coordinate = MapCoordinate(36.4881, 127.7256),
                            ),
                        ),
                    ),
                    CourseDay(
                        dayNumber = 2,
                        places = listOf(
                            CoursePlace(
                                placeId = "boeun-beopjusa",
                                name = "법주사",
                                imageUrl = "https://images.unsplash.com/photo-1518182170546-07661fd94144?auto=format&fit=crop&w=900&q=85",
                                tags = listOf("08:00~18:00", "연중무휴", "주차가능"),
                                coordinate = MapCoordinate(36.5422, 127.8330),
                            ),
                            CoursePlace(
                                placeId = "boeun-jeongipumsong",
                                name = "정이품송",
                                imageUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=900&q=85",
                                tags = listOf("상시 개방", "주차가능"),
                                coordinate = MapCoordinate(36.5044, 127.8317),
                            ),
                        ),
                    ),
                ),
            ),
            Course(
                courseId = "course-gangneung",
                regionName = "강원 강릉권",
                days = listOf(
                    CourseDay(
                        dayNumber = 1,
                        places = listOf(
                            CoursePlace(
                                placeId = "gangneung-anmok-beach",
                                name = "안목해변",
                                imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=900&q=85",
                                tags = listOf("상시 개방", "주차가능"),
                                coordinate = MapCoordinate(37.7691, 128.9485),
                            ),
                            CoursePlace(
                                placeId = "gangneung-myeongjudong",
                                name = "명주동 골목",
                                imageUrl = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=900&q=85",
                                tags = listOf("상시 개방"),
                                coordinate = MapCoordinate(37.7515, 128.8914),
                            ),
                        ),
                    ),
                    CourseDay(
                        dayNumber = 2,
                        places = listOf(
                            CoursePlace(
                                placeId = "gangneung-gyeongpo-lake",
                                name = "경포호수",
                                imageUrl = "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?auto=format&fit=crop&w=900&q=85",
                                tags = listOf("상시 개방", "주차가능"),
                                coordinate = MapCoordinate(37.7948, 128.8957),
                            ),
                            CoursePlace(
                                placeId = "gangneung-jumunjin-beach",
                                name = "주문진 해변",
                                imageUrl = "https://images.unsplash.com/photo-1476673160081-cf065607f449?auto=format&fit=crop&w=900&q=85",
                                tags = listOf("상시 개방", "주차가능"),
                                coordinate = MapCoordinate(37.8916, 128.8287),
                            ),
                        ),
                    ),
                ),
            ),
            Course(
                courseId = "course-jeju",
                regionName = "제주 서부권",
                days = listOf(
                    CourseDay(
                        dayNumber = 1,
                        places = listOf(
                            CoursePlace(
                                placeId = "jeju-hyeopjae-beach",
                                name = "협재해수욕장",
                                imageUrl = "https://images.unsplash.com/photo-1439853949127-fa647821eba0?auto=format&fit=crop&w=900&q=85",
                                tags = listOf("상시 개방", "주차가능"),
                                coordinate = MapCoordinate(33.3946, 126.2398),
                            ),
                            CoursePlace(
                                placeId = "jeju-geumneung-coast",
                                name = "금능해안",
                                imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=900&q=85",
                                tags = listOf("상시 개방", "주차가능"),
                                coordinate = MapCoordinate(33.3907, 126.2353),
                            ),
                        ),
                    ),
                    CourseDay(
                        dayNumber = 2,
                        places = listOf(
                            CoursePlace(
                                placeId = "jeju-osulloc",
                                name = "오설록 티 뮤지엄",
                                imageUrl = "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=900&q=85",
                                tags = listOf("09:00~18:00", "연중무휴", "주차가능"),
                                coordinate = MapCoordinate(33.3059, 126.2898),
                            ),
                            CoursePlace(
                                placeId = "jeju-saebyeol-oreum",
                                name = "새별오름",
                                imageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=900&q=85",
                                tags = listOf("상시 개방", "주차가능"),
                                coordinate = MapCoordinate(33.3667, 126.3579),
                            ),
                        ),
                    ),
                ),
            ),
        ).associateBy(Course::courseId)
    }
}

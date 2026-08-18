package com.example.sairo14.data.repository.fake

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.DiscoveryImages
import com.example.sairo14.domain.model.HomeContent
import com.example.sairo14.domain.model.SavedTripSummary
import com.example.sairo14.domain.repository.HomeRepository
import javax.inject.Inject
import javax.inject.Singleton

/** 서버 연동 전 Home 캔버스를 확인할 수 있도록 고정 샘플 데이터를 제공한다. */
@Singleton
class FakeHomeRepository @Inject constructor() : HomeRepository {

    override suspend fun getHomeContent(): AppResult<HomeContent> =
        AppResult.Success(
            HomeContent(
                discoveryImages = DiscoveryImages(
                    backImageUrl = null,
                    frontImageUrl = null,
                ),
                // 캔버스 UI를 다시 확인하려면 `sampleSavedTrips`로 변경한다.
                savedTrips = emptyList(),
            ),
        )

    private companion object {
        val sampleSavedTrips = listOf(
            SavedTripSummary(
                savedTripId = "saved-trip-seongsu",
                courseId = "course-seongsu",
                regionName = "성수",
                thumbnailImageUrl = "https://images.unsplash.com/photo-1598301257982-0cf014dabbcd?auto=format&fit=crop&w=600&q=85",
            ),
            SavedTripSummary(
                savedTripId = "saved-trip-yeonnam",
                courseId = "course-yeonnam",
                regionName = "연남",
                thumbnailImageUrl = "https://images.unsplash.com/photo-1519608487953-e999c86e7454?auto=format&fit=crop&w=600&q=85",
            ),
            SavedTripSummary(
                savedTripId = "saved-trip-haebangchon",
                courseId = "course-haebangchon",
                regionName = "해방촌",
                thumbnailImageUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=600&q=85",
            ),
            SavedTripSummary(
                savedTripId = "saved-trip-euljiro",
                courseId = "course-euljiro",
                regionName = "을지로",
                thumbnailImageUrl = "https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=600&q=85",
            ),
        )
    }
}

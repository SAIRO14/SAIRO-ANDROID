package com.example.sairo14.data.repository

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.DiscoveryImages
import com.example.sairo14.domain.model.HomeContent
import com.example.sairo14.domain.model.SavedTrip
import com.example.sairo14.domain.model.SavedTripSummary
import com.example.sairo14.domain.repository.HomeRepository
import com.example.sairo14.domain.repository.SavedTripRepository
import javax.inject.Inject
import javax.inject.Singleton

/** 실제 저장 여행지 목록을 홈 화면 콘텐츠로 조립한다. */
@Singleton
class DefaultHomeRepository @Inject constructor(
    private val savedTripRepository: SavedTripRepository,
) : HomeRepository {

    override suspend fun getHomeContent(): AppResult<HomeContent> =
        when (val result = savedTripRepository.getSavedTrips(size = HomeSavedTripLimit)) {
            is AppResult.Success -> AppResult.Success(
                HomeContent(
                    discoveryImages = DiscoveryImages(
                        backImageUrl = null,
                        frontImageUrl = null,
                    ),
                    savedTrips = result.value.items.map(SavedTrip::toHomeSummary),
                ),
            )

            is AppResult.Failure -> result
        }
}

private fun SavedTrip.toHomeSummary(): SavedTripSummary = SavedTripSummary(
    savedTripId = savedTripId,
    courseId = courseId,
    regionName = regionName,
    thumbnailImageUrl = imageUrl?.takeIf(String::isNotBlank)
        ?: spotImageUrls.firstOrNull(String::isNotBlank),
)

private const val HomeSavedTripLimit = 8

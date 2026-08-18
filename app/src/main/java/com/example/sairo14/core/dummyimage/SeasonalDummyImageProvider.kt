package com.example.sairo14.core.dummyimage

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.example.sairo14.R
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/** 로컬 여행 더미 이미지를 분류하는 계절이다. */
enum class DummyImageSeason {
    Spring,
    Summer,
    Autumn,
    Winter,
}

/** 겹친 카드의 뒤·앞 레이어에 사용할 로컬 이미지 한 쌍이다. */
@Immutable
data class DummyImagePair(
    @DrawableRes val backImageRes: Int,
    @DrawableRes val frontImageRes: Int,
)

/** 온보딩과 홈이 같은 날짜에 공유할 로컬 더미 이미지 배정 결과다. */
@Immutable
data class SeasonalDummyImageSet(
    val onboardingPairs: List<DummyImagePair>,
    val homePair: DummyImagePair,
)

/** 현재 계절의 로컬 더미 이미지를 중복 없이 화면별로 새로 배정한다. */
@Singleton
class SeasonalDummyImageProvider @Inject constructor() {
    /** 호출할 때마다 현재 계절 이미지와 공통 이미지로 새 카드 배정을 생성한다. */
    fun createImageSet(): SeasonalDummyImageSet {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1

        return SeasonalDummyImageSelector.select(
            season = seasonOf(month),
            random = Random.Default,
        )
    }
}

internal fun seasonOf(month: Int): DummyImageSeason = when (month) {
    in 3..5 -> DummyImageSeason.Spring
    in 6..8 -> DummyImageSeason.Summer
    in 9..11 -> DummyImageSeason.Autumn
    12, 1, 2 -> DummyImageSeason.Winter
    else -> error("유효하지 않은 월입니다: $month")
}

internal object SeasonalDummyImageSelector {
    /** 계절·공통 이미지에서 각각 네 장씩 골라 온보딩 여섯 장과 홈 두 장으로 나눈다. */
    fun select(
        season: DummyImageSeason,
        random: Random,
    ): SeasonalDummyImageSet {
        val seasonalImages = SeasonalDummyImageCatalog.imagesFor(season).distinct()
        val commonImages = SeasonalDummyImageCatalog.commonImages.distinct()
        require(seasonalImages.size >= RequiredImagesPerCategory)
        require(commonImages.size >= RequiredImagesPerCategory)

        val selectedSeasonal = seasonalImages.shuffled(random).take(RequiredImagesPerCategory)
        val selectedCommon = commonImages.shuffled(random).take(RequiredImagesPerCategory)
        val onboardingImages = (
            selectedSeasonal.take(OnboardingImagesPerCategory) +
                selectedCommon.take(OnboardingImagesPerCategory)
            ).shuffled(random)
        val homeImages = (
            selectedSeasonal.drop(OnboardingImagesPerCategory) +
                selectedCommon.drop(OnboardingImagesPerCategory)
            ).shuffled(random)

        return SeasonalDummyImageSet(
            onboardingPairs = onboardingImages.chunked(ImagesPerPair).map { images ->
                DummyImagePair(
                    backImageRes = images[0],
                    frontImageRes = images[1],
                )
            },
            homePair = DummyImagePair(
                backImageRes = homeImages[0],
                frontImageRes = homeImages[1],
            ),
        )
    }
}

private object SeasonalDummyImageCatalog {
    val commonImages = listOf(
        R.drawable.dummy_travel_common_01,
        R.drawable.dummy_travel_common_02,
        R.drawable.dummy_travel_common_03,
        R.drawable.dummy_travel_common_04,
        R.drawable.dummy_travel_common_05,
        R.drawable.dummy_travel_common_06,
        R.drawable.dummy_travel_common_07,
        R.drawable.dummy_travel_common_08,
        R.drawable.dummy_travel_common_09,
        R.drawable.dummy_travel_common_10,
        R.drawable.dummy_travel_common_11,
        R.drawable.dummy_travel_common_12,
    )

    fun imagesFor(season: DummyImageSeason): List<Int> = when (season) {
        DummyImageSeason.Spring -> springImages
        DummyImageSeason.Summer -> summerImages
        DummyImageSeason.Autumn -> autumnImages
        DummyImageSeason.Winter -> winterImages
    }

    private val springImages = listOf(
        R.drawable.dummy_travel_spring_01,
        R.drawable.dummy_travel_spring_02,
        R.drawable.dummy_travel_spring_03,
        R.drawable.dummy_travel_spring_04,
        R.drawable.dummy_travel_spring_05,
        R.drawable.dummy_travel_spring_06,
        R.drawable.dummy_travel_spring_07,
    )
    private val summerImages = listOf(
        R.drawable.dummy_travel_summer_01,
        R.drawable.dummy_travel_summer_02,
        R.drawable.dummy_travel_summer_03,
        R.drawable.dummy_travel_summer_04,
        R.drawable.dummy_travel_summer_05,
        R.drawable.dummy_travel_summer_06,
    )
    private val autumnImages = listOf(
        R.drawable.dummy_travel_autumn_01,
        R.drawable.dummy_travel_autumn_02,
        R.drawable.dummy_travel_autumn_03,
        R.drawable.dummy_travel_autumn_04,
        R.drawable.dummy_travel_autumn_05,
        R.drawable.dummy_travel_autumn_06,
    )
    private val winterImages = listOf(
        R.drawable.dummy_travel_winter_01,
        R.drawable.dummy_travel_winter_02,
        R.drawable.dummy_travel_winter_03,
        R.drawable.dummy_travel_winter_04,
        R.drawable.dummy_travel_winter_05,
        R.drawable.dummy_travel_winter_06,
        R.drawable.dummy_travel_winter_07,
    )
}

private const val RequiredImagesPerCategory = 4
private const val OnboardingImagesPerCategory = 3
private const val ImagesPerPair = 2

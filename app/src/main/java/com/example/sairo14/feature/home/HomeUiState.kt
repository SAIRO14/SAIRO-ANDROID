package com.example.sairo14.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.example.sairo14.R
import com.example.sairo14.domain.model.AppError

/** 홈 화면이 렌더링할 로딩, 콘텐츠, 오류 상태를 나타낸다. */
sealed interface HomeUiState {
    /** 중앙 탐색 이미지와 저장 여행지 목록을 불러오는 중인 상태다. */
    data object Loading : HomeUiState

    /** 홈 화면에 필요한 탐색 이미지와 저장 여행지 요약 정보를 표시할 수 있는 상태다. */
    @Immutable
    data class Content(
        val discoveryImages: HomeDiscoveryImagesUiModel = HomeDiscoveryImagesUiModel(),
        val savedTrips: List<HomeSavedTripUiModel> = emptyList(),
    ) : HomeUiState

    /** 홈 데이터를 불러오지 못해 오류 종류에 맞는 사용자 안내가 필요한 상태다. */
    data class Error(
        val error: AppError,
    ) : HomeUiState
}

/** 홈 중앙 탐색 CTA의 앞·뒤 사진 URL을 화면에 맞게 전달한다. */
@Immutable
data class HomeDiscoveryImagesUiModel(
    val backImageUrl: String? = null,
    val frontImageUrl: String? = null,
    @DrawableRes val backFallbackRes: Int = R.drawable.img_dummy_view,
    @DrawableRes val frontFallbackRes: Int = R.drawable.img_dummy_view,
)

/** 저장한 여행지 카드 하나를 그리는 데 필요한 UI 전용 요약 정보다. */
@Immutable
data class HomeSavedTripUiModel(
    val savedTripId: String,
    val courseId: String,
    val regionName: String,
    val thumbnailImageUrl: String? = null,
)

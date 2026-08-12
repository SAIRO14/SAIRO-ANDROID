package com.example.sairo14.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Sairo 앱에서 백스택에 저장할 수 있는 Nav3 목적지의 공통 계약이다. */
@Serializable
sealed interface SairoRoute : NavKey

/** 여행지 탐색 전의 홈 화면을 식별한다. */
@Serializable
data object HomeRoute : SairoRoute

/** 현재 사용자가 저장한 여행지 목록을 식별한다. */
@Serializable
data object SavedTripsRoute : SairoRoute

/** 코스 ID에 해당하는 일차별 지도와 장소 목록 화면을 식별한다. */
@Serializable
data class TravelDetailRoute(
    val courseId: String,
) : SairoRoute

/** 온보딩 인트로 화면의 진입 출처를 구분한다. */
@Serializable
enum class OnboardingIntroEntryPoint {
    /** 앱 최초 실행 과정에서 표시하는 인트로다. */
    AppStart,

    /** Home의 여행지 탐색 CTA에서 다시 진입한 인트로다. */
    Home,
}

/** 온보딩에서 여행지 찾기 서비스와 시작 행동을 소개하는 첫 화면을 식별한다. */
@Serializable
data class OnboardingIntroRoute(
    val entryPoint: OnboardingIntroEntryPoint = OnboardingIntroEntryPoint.AppStart,
) : SairoRoute

/** 온보딩 탐색 세션에서 취향 사진을 선택하는 화면을 식별한다. */
@Serializable
data class OnboardingPhotoSelectRoute(
    val searchSessionId: String,
) : SairoRoute

/** 온보딩 탐색 세션에서 선택한 사진을 분석하며 카드 스태킹 모션을 표시하는 화면을 식별한다. */
@Serializable
data class OnboardingLoadingRoute(
    val searchSessionId: String,
    val selectedPhotoIds: List<String>,
    val animationPhotos: List<OnboardingAnimationPhoto>,
) : SairoRoute

/** 온보딩 로딩 애니메이션 카드에 표시할 선택 사진 정보다. */
@Serializable
data class OnboardingAnimationPhoto(
    val id: String,
    val imageUrl: String,
)

/** 온보딩 탐색 세션의 선택 사진에 따른 추천 결과를 식별한다. */
@Serializable
data class OnboardingResultRoute(
    val searchSessionId: String,
    val selectedPhotoIds: List<String>,
) : SairoRoute

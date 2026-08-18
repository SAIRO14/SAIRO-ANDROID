package com.example.sairo14.feature.onboarding.intro

import com.example.sairo14.R
import com.example.sairo14.core.dummyimage.DummyImagePair

/** 온보딩 인트로 화면에 표시할 세 쌍의 로컬 이미지 상태를 나타낸다. */
data class OnboardingIntroUiState(
    val imagePairs: List<DummyImagePair> = List(DefaultImagePairCount) {
        DummyImagePair(
            backImageRes = R.drawable.img_dummy_view,
            frontImageRes = R.drawable.img_dummy_view,
        )
    },
)

private const val DefaultImagePairCount = 3

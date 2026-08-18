package com.example.sairo14.feature.traveldetail

import com.example.sairo14.domain.model.AppError

/** 여행 상세 화면에서 한 번만 처리할 외부 동작과 사용자 안내를 나타낸다. */
sealed interface TravelDetailEffect {

    /** 코스 공유 링크가 준비되어 시스템 공유 화면을 열어야 한다.
     * @param regionName 공유 문구에 표시할 여행 지역명
     * @param shareUrl 공유할 읽기 전용 코스 URL
     */
    data class OpenShareSheet(
        val regionName: String,
        val shareUrl: String,
    ) : TravelDetailEffect

    /** 공유 링크 요청이 실패해 현재 콘텐츠 위에 안내가 필요하다.
     * @param error Data 계층이 Domain 오류로 변환한 실패 원인
     */
    data class ShowShareError(
        val error: AppError,
    ) : TravelDetailEffect
}

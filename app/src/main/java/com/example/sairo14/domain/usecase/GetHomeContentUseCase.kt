package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.HomeContent
import com.example.sairo14.domain.repository.HomeRepository
import javax.inject.Inject

/** 홈 화면에 표시할 콘텐츠를 조회한다. */
class GetHomeContentUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
) {
    suspend operator fun invoke(): AppResult<HomeContent> = homeRepository.getHomeContent()
}

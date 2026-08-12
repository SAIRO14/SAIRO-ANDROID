package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.PhotoCandidate
import com.example.sairo14.domain.repository.PhotoSelectionRepository
import javax.inject.Inject

/** 온보딩 사진 선택 화면에 표시할 사진 후보 40장을 조회한다. */
class GetPhotoCandidatesUseCase @Inject constructor(
    private val photoSelectionRepository: PhotoSelectionRepository,
) {
    suspend operator fun invoke(): AppResult<List<PhotoCandidate>> =
        photoSelectionRepository.getPhotoCandidates(limit = OnboardingPhotoCandidateLimit)
}

private const val OnboardingPhotoCandidateLimit = 40

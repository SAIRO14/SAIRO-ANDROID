package com.example.sairo14.data.repository.fake

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.PhotoCandidate
import com.example.sairo14.domain.repository.PhotoSelectionRepository
import javax.inject.Inject
import javax.inject.Singleton

/** 서버 연동 전 온보딩 사진 선택 흐름을 확인할 수 있도록 고정 사진 후보를 제공한다. */
@Singleton
class FakePhotoSelectionRepository @Inject constructor() : PhotoSelectionRepository {

    override suspend fun getPhotoCandidates(limit: Int): AppResult<List<PhotoCandidate>> =
        AppResult.Success(photoCandidates)

    private companion object {
        val photoCandidates = listOf(
            PhotoCandidate(
                id = "photo-seongsu-tree",
                imageUrl = "https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=900&q=85",
                contentDescription = "나무가 드리운 산책로",
            ),
            PhotoCandidate(
                id = "photo-jeju-coast",
                imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=900&q=85",
                contentDescription = "푸른 바다와 해변",
            ),
            PhotoCandidate(
                id = "photo-busan-night",
                imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=900&q=85",
                contentDescription = "도시의 밤 풍경",
            ),
            PhotoCandidate(
                id = "photo-forest-cabin",
                imageUrl = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=900&q=85",
                contentDescription = "햇빛이 비치는 숲",
            ),
            PhotoCandidate(
                id = "photo-quiet-lake",
                imageUrl = "https://images.unsplash.com/photo-1439853949127-fa647821eba0?auto=format&fit=crop&w=900&q=85",
                contentDescription = "고요한 호수와 산",
            ),
            PhotoCandidate(
                id = "photo-village-street",
                imageUrl = "https://images.unsplash.com/photo-1467269204594-9661b134dd2b?auto=format&fit=crop&w=900&q=85",
                contentDescription = "따뜻한 색감의 거리",
            ),
            PhotoCandidate(
                id = "photo-mountain-view",
                imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=900&q=85",
                contentDescription = "구름 위의 산 능선",
            ),
            PhotoCandidate(
                id = "photo-cafe-window",
                imageUrl = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=900&q=85",
                contentDescription = "창가의 여유로운 카페",
            ),
            PhotoCandidate(
                id = "photo-autumn-road",
                imageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=900&q=85",
                contentDescription = "가을빛이 감도는 길",
            ),
            PhotoCandidate(
                id = "photo-sunrise-field",
                imageUrl = "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=900&q=85",
                contentDescription = "햇살이 비치는 들판",
            ),
        )
    }
}

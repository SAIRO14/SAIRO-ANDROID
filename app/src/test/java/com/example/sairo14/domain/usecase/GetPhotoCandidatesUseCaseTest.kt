package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.PhotoCandidate
import com.example.sairo14.domain.repository.PhotoSelectionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPhotoCandidatesUseCaseTest {

    @Test
    fun `온보딩 사진 후보를 40장 요청한다`() = runTest {
        val repository = RecordingPhotoSelectionRepository()

        GetPhotoCandidatesUseCase(repository)()

        assertEquals(40, repository.requestedLimit)
    }

    private class RecordingPhotoSelectionRepository : PhotoSelectionRepository {
        var requestedLimit: Int? = null
            private set

        override suspend fun getPhotoCandidates(limit: Int): AppResult<List<PhotoCandidate>> {
            requestedLimit = limit
            return AppResult.Success(emptyList())
        }
    }
}

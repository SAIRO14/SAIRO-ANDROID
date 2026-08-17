package com.example.sairo14.data.repository

import com.example.sairo14.core.datastore.DeviceIdProvider
import com.example.sairo14.data.repository.fake.FakeSavedTripRepository
import com.example.sairo14.domain.model.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FakeSavedTripRepositoryTest {

    @Test
    fun `저장 여행지를 삭제하면 같은 익명 사용자의 이후 목록에서 사라진다`() = runTest {
        val repository = FakeSavedTripRepository(TestDeviceIdProvider("device-a"))
        val initialTrips = repository.getSavedTrips().successValue().items
        val removedTripId = initialTrips.first().savedTripId

        repository.deleteSavedTrip(removedTripId)
        val updatedTrips = repository.getSavedTrips().successValue().items

        assertEquals(initialTrips.size - 1, updatedTrips.size)
        assertFalse(updatedTrips.any { trip -> trip.savedTripId == removedTripId })
    }

    @Test
    fun `저장 여행지 삭제는 다른 익명 사용자의 목록에 영향을 주지 않는다`() = runTest {
        val deviceIdProvider = TestDeviceIdProvider("device-a")
        val repository = FakeSavedTripRepository(deviceIdProvider)
        val removedTripId = repository.getSavedTrips().successValue().items.first().savedTripId

        repository.deleteSavedTrip(removedTripId)
        deviceIdProvider.deviceId = "device-b"
        val secondDeviceTrips = repository.getSavedTrips().successValue().items

        assertFalse(secondDeviceTrips.isEmpty())
        assertEquals(removedTripId, secondDeviceTrips.first().savedTripId)
    }

    @Test
    fun `다음 커서로 조회하면 최신 저장 순서를 유지한 다음 페이지를 반환한다`() = runTest {
        val repository = FakeSavedTripRepository(TestDeviceIdProvider("device-a"))

        val firstPage = repository.getSavedTrips(size = 2).successValue()
        val secondPage = repository.getSavedTrips(cursor = firstPage.nextCursor, size = 2).successValue()

        assertEquals(listOf("saved-trip-boeun", "saved-trip-gangneung"), firstPage.items.map { it.savedTripId })
        assertEquals("fake-saved-trip-cursor-2", firstPage.nextCursor)
        assertEquals(listOf("saved-trip-jeju"), secondPage.items.map { it.savedTripId })
        assertEquals(null, secondPage.nextCursor)
    }

    @Test
    fun `모든 저장 여행지를 삭제한 뒤 첫 페이지를 조회하면 빈 페이지를 반환한다`() = runTest {
        val repository = FakeSavedTripRepository(TestDeviceIdProvider("device-a"))
        val savedTripIds = repository.getSavedTrips().successValue().items.map { it.savedTripId }

        savedTripIds.forEach { savedTripId ->
            repository.deleteSavedTrip(savedTripId)
        }

        val emptyPage = repository.getSavedTrips().successValue()

        assertEquals(emptyList<com.example.sairo14.domain.model.SavedTrip>(), emptyPage.items)
        assertEquals(null, emptyPage.nextCursor)
    }

    @Test
    fun `유효하지 않은 커서와 페이지 크기는 실패로 반환한다`() = runTest {
        val repository = FakeSavedTripRepository(TestDeviceIdProvider("device-a"))

        assertEquals(
            AppResult.Failure(com.example.sairo14.domain.model.AppError.InvalidCursor),
            repository.getSavedTrips(cursor = "invalid", size = 2),
        )
        assertEquals(
            AppResult.Failure(com.example.sairo14.domain.model.AppError.InvalidRequest),
            repository.getSavedTrips(size = 0),
        )
    }

    private fun <T> AppResult<T>.successValue(): T =
        (this as? AppResult.Success<T>)?.value
            ?: error("성공 결과를 기대했습니다.")

    private class TestDeviceIdProvider(
        var deviceId: String,
    ) : DeviceIdProvider {
        override suspend fun getDeviceId(): String = deviceId
    }
}

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

    private fun <T> AppResult<T>.successValue(): T =
        (this as? AppResult.Success<T>)?.value
            ?: error("성공 결과를 기대했습니다.")

    private class TestDeviceIdProvider(
        var deviceId: String,
    ) : DeviceIdProvider {
        override suspend fun getDeviceId(): String = deviceId
    }
}

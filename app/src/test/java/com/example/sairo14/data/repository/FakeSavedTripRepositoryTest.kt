package com.example.sairo14.data.repository

import com.example.sairo14.domain.model.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FakeSavedTripRepositoryTest {

    @Test
    fun `저장 여행지를 삭제하면 같은 익명 사용자의 이후 목록에서 사라진다`() = runTest {
        val repository = FakeSavedTripRepository()
        val deviceId = "device-a"
        val initialTrips = repository.getSavedTrips(deviceId).successValue()
        val removedTripId = initialTrips.first().savedTripId

        repository.deleteSavedTrip(deviceId, removedTripId)
        val updatedTrips = repository.getSavedTrips(deviceId).successValue()

        assertEquals(initialTrips.size - 1, updatedTrips.size)
        assertFalse(updatedTrips.any { trip -> trip.savedTripId == removedTripId })
    }

    @Test
    fun `저장 여행지 삭제는 다른 익명 사용자의 목록에 영향을 주지 않는다`() = runTest {
        val repository = FakeSavedTripRepository()
        val firstDeviceId = "device-a"
        val secondDeviceId = "device-b"
        val removedTripId = repository.getSavedTrips(firstDeviceId).successValue().first().savedTripId

        repository.deleteSavedTrip(firstDeviceId, removedTripId)
        val secondDeviceTrips = repository.getSavedTrips(secondDeviceId).successValue()

        assertFalse(secondDeviceTrips.isEmpty())
        assertEquals(removedTripId, secondDeviceTrips.first().savedTripId)
    }

    private fun <T> AppResult<T>.successValue(): T =
        (this as? AppResult.Success<T>)?.value
            ?: error("성공 결과를 기대했습니다.")
}

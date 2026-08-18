package com.example.sairo14.feature.bookmark

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkChangeNotifierTest {

    @Test
    fun `느린 수집자에게 연속 변경을 보내도 모든 변경을 순서대로 전달한다`() = runTest {
        val notifier = BookmarkChangeNotifier()
        val receivedChanges = mutableListOf<BookmarkChange>()
        val firstChangeReceived = CompletableDeferred<Unit>()
        val continueCollection = Channel<Unit>(Channel.UNLIMITED)
        val collector = launch {
            notifier.changes.collect { change ->
                receivedChanges += change
                if (receivedChanges.size == 1) firstChangeReceived.complete(Unit)
                continueCollection.receive()
            }
        }
        runCurrent()

        val firstChange = change(courseId = "course-1", isSaved = true)
        val secondChange = change(courseId = "course-2", isSaved = true)
        val thirdChange = change(courseId = "course-1", isSaved = false)

        val firstEmission = async { notifier.notify(firstChange) }
        firstChangeReceived.await()
        firstEmission.await()

        val secondEmission = async { notifier.notify(secondChange) }
        runCurrent()
        val thirdEmission = async { notifier.notify(thirdChange) }
        runCurrent()

        assertFalse(thirdEmission.isCompleted)

        continueCollection.send(Unit)
        secondEmission.await()
        thirdEmission.await()
        continueCollection.send(Unit)
        continueCollection.send(Unit)
        runCurrent()

        assertEquals(listOf(firstChange, secondChange, thirdChange), receivedChanges)
        collector.cancel()
    }

    private fun change(courseId: String, isSaved: Boolean) = BookmarkChange(
        courseId = courseId,
        isSaved = isSaved,
        savedTripId = if (isSaved) "saved-trip-$courseId" else null,
    )
}

package com.example.myapplication

import com.example.myapplication.events.Event
import com.example.myapplication.events.EventRules
import com.example.myapplication.events.EventStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventRulesTest {

    private val now = 1_000_000L

    private fun validate(
        title: String = "Hackathon",
        venue: String = "Hall A",
        start: Long = now + 1_000,
        end: Long = now + 2_000,
        capacity: Int = 50,
        points: Int = 5
    ) = EventRules.validate(title, venue, start, end, capacity, points, now)

    @Test fun validEvent_hasNoError() = assertNull(validate())

    @Test fun blankTitle_isRejected() = assertEquals("Title is required", validate(title = "  "))

    @Test fun blankVenue_isRejected() = assertEquals("Venue is required", validate(venue = ""))

    @Test fun startInThePast_isRejected() = assertNotNull(validate(start = now - 1, end = now + 5))

    @Test fun endBeforeStart_isRejected() =
        assertEquals("End time must be after the start time", validate(start = now + 2_000, end = now + 1_000))

    @Test fun endEqualToStart_isRejected() = assertNotNull(validate(start = now + 1_000, end = now + 1_000))

    @Test fun zeroCapacity_isRejected() = assertNotNull(validate(capacity = 0))

    @Test fun overMaxCapacity_isRejected() = assertNotNull(validate(capacity = EventRules.MAX_CAPACITY + 1))

    @Test fun zeroPoints_isRejected() = assertNotNull(validate(points = 0))

    @Test fun onlyPendingEventsCanBeEdited() {
        assertTrue(EventRules.canEdit(EventStatus.PENDING))
        assertFalse(EventRules.canEdit(EventStatus.APPROVED))
        assertFalse(EventRules.canEdit(EventStatus.REJECTED))
        assertFalse(EventRules.canEdit(EventStatus.CANCELLED))
    }

    @Test fun onlyPendingOrApprovedCanBeCancelled() {
        assertTrue(EventRules.canCancel(EventStatus.PENDING))
        assertTrue(EventRules.canCancel(EventStatus.APPROVED))
        assertFalse(EventRules.canCancel(EventStatus.REJECTED))
        assertFalse(EventRules.canCancel(EventStatus.CANCELLED))
    }

    @Test fun search_matchesTitleVenueAndIgnoresCase() {
        val events = listOf(
            Event(id = "1", title = "Chess Night", venue = "Hall A"),
            Event(id = "2", title = "Hackathon", venue = "Lab 3")
        )
        assertEquals(listOf("1"), EventRules.search(events, "chess").map { it.id })
        assertEquals(listOf("2"), EventRules.search(events, "LAB").map { it.id })
        assertEquals(2, EventRules.search(events, "  ").size)
        assertTrue(EventRules.search(events, "zzz").isEmpty())
    }

    @Test fun isFull_whenBookedReachesCapacity() {
        assertFalse(EventRules.isFull(Event(capacity = 2, bookedCount = 1)))
        assertTrue(EventRules.isFull(Event(capacity = 2, bookedCount = 2)))
    }
}
package com.example.myapplication

import com.example.myapplication.events.Event
import com.example.myapplication.events.EventField
import com.example.myapplication.events.FileKind
import com.example.myapplication.events.FileUpload
import com.example.myapplication.events.EventRules
import com.example.myapplication.events.EventSession
import com.example.myapplication.events.EventStatus
import com.example.myapplication.events.NewEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventRulesTest {

    private val now = 1_000_000L

    private fun event(
        title: String = "Hackathon",
        venue: String = "Hall A",
        start: Long = now + 1_000,
        end: Long = now + 2_000,
        sessions: List<EventSession> = listOf(EventSession(start, end)),
        capacity: Int = 50,
        points: Int = 5,
        target: Int = 10,
        category: String = "Technology",
        activityType: String = "Competition",
        memberPrice: Double = 0.0,
        nonMemberPrice: Double = 0.0
    ) = NewEvent(
        title = title,
        description = "",
        venue = venue,
        startTime = start,
        endTime = end,
        capacity = capacity,
        pointsRequested = points,
        sessions = sessions,
        memberPrice = memberPrice,
        nonMemberPrice = nonMemberPrice,
        targetParticipants = target,
        category = category,
        activityType = activityType
    )

    // All errors, by field
    private fun errors(event: NewEvent = event()) = EventRules.validate(event, now)

    // The first error, or null if the event is valid
    private fun validate(
        title: String = "Hackathon",
        venue: String = "Hall A",
        start: Long = now + 1_000,
        end: Long = now + 2_000,
        capacity: Int = 50,
        points: Int = 5
    ): String? {
        val errors = errors(event(title, venue, start, end, capacity = capacity, points = points))
        for (message in errors.values) {
            return message
        }
        return null
    }

    @Test fun validEvent_hasNoError() = assertNull(validate())

    @Test fun blankTitle_isRejected() = assertEquals("Title is required", validate(title = "  "))

    @Test fun blankVenue_isRejected() = assertEquals("Venue is required", validate(venue = ""))

    @Test fun startInThePast_isRejected() = assertNotNull(validate(start = now - 1, end = now + 5))

    @Test fun endBeforeStart_isRejected() =
        assertEquals("End time must be after the start time", validate(start = now + 2_000, end = now + 1_000))

    @Test fun endEqualToStart_isRejected() = assertNotNull(validate(start = now + 1_000, end = now + 1_000))

    @Test fun zeroCapacity_isRejected() = assertNotNull(validate(capacity = 0))

    @Test fun overMaxCapacity_isRejected() = assertNotNull(validate(capacity = EventRules.MAX_CAPACITY + 1))

    @Test fun zeroPoints_isAllowed() = assertNull(validate(points = 0))

    @Test fun negativePoints_isRejected() = assertNotNull(validate(points = -1))

    @Test fun overMaxPoints_isRejected() = assertNotNull(validate(points = EventRules.MAX_POINTS + 1))

    @Test fun noDates_isRejected() =
        assertEquals("Add at least one date", errors(event(sessions = emptyList()))[EventField.START])

    @Test fun dateInThePast_isRejected() {
        val past = EventSession(now - 2_000, now - 1_000)
        val future = EventSession(now + 1_000, now + 2_000)
        assertEquals(
            "Every date must be in the future",
            errors(event(sessions = listOf(future, past)))[EventField.START]
        )
    }

    @Test fun dateEndBeforeStart_isRejected() {
        val backwards = EventSession(now + 3_000, now + 2_500)
        assertEquals(
            "Each end time must be after its start time",
            errors(event(end = now + 4_000, sessions = listOf(EventSession(now + 1_000, now + 2_000), backwards)))[EventField.END]
        )
    }

    @Test fun zeroTarget_isRejected() =
        assertEquals("Target participants must be at least 1", errors(event(target = 0))[EventField.TARGET_PARTICIPANTS])

    @Test fun bigTarget_isAllowed() = assertTrue(errors(event(target = 5_000)).isEmpty())

    @Test fun blankCategory_isRejected() =
        assertEquals("Pick a category", errors(event(category = ""))[EventField.CATEGORY])

    @Test fun blankActivityType_isRejected() =
        assertEquals("Pick an activity type", errors(event(activityType = " "))[EventField.ACTIVITY_TYPE])

    @Test fun negativePrice_isRejected() =
        assertTrue(errors(event(memberPrice = -1.0)).containsKey(EventField.MEMBER_PRICE))

    @Test fun overMaxPrice_isRejected() =
        assertTrue(errors(event(nonMemberPrice = EventRules.MAX_PRICE + 1)).containsKey(EventField.NON_MEMBER_PRICE))

    @Test fun threeDecimalPrice_isRejected() =
        assertEquals(
            "Member price can have at most 2 decimal places",
            errors(event(memberPrice = 12.345))[EventField.MEMBER_PRICE]
        )

    @Test fun senPrice_isAllowed() = assertTrue(errors(event(memberPrice = 12.50, nonMemberPrice = 0.05)).isEmpty())

    @Test fun freeEvent_isAllowed() = assertTrue(errors(event(memberPrice = 0.0, nonMemberPrice = 0.0)).isEmpty())

    @Test fun everyWrongField_isReportedAtOnce() {
        val errors = errors(event(title = "", venue = "", target = 0, category = ""))
        assertTrue(errors.containsKey(EventField.TITLE))
        assertTrue(errors.containsKey(EventField.VENUE))
        assertTrue(errors.containsKey(EventField.TARGET_PARTICIPANTS))
        assertTrue(errors.containsKey(EventField.CATEGORY))
        assertEquals(4, errors.size)
    }

    @Test fun epf_isNeeded() {
        assertFalse(EventRules.hasEpf(emptyList()))
        assertFalse(EventRules.hasEpf(listOf(FileUpload(FileKind.ATTACHMENT, "budget.xlsx", "", ""))))
        assertTrue(EventRules.hasEpf(listOf(FileUpload(FileKind.EPF, "epf.pdf", "application/pdf", ""))))
    }

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
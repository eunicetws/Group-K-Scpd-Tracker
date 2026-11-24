package com.example.myapplication

import com.example.myapplication.events.EventField
import com.example.myapplication.events.EventForm
import com.example.myapplication.events.EventFormInput
import com.example.myapplication.events.EventFormResult
import com.example.myapplication.events.EventMedia
import com.example.myapplication.events.EventRules
import com.example.myapplication.events.EventSession
import com.example.myapplication.events.FileKind
import com.example.myapplication.events.FileUpload
import com.example.myapplication.events.NewEvent
import com.example.myapplication.events.PosterUpload
import com.example.myapplication.ui.events.CreateEventViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class CreateEventTest {

    private val validForm = EventFormInput(
        title = "Music Festival",
        venue = "F3C09",
        start = "2099-10-24 12:00",
        end = "2099-10-24 13:00",
        capacity = "50",
        points = "2",
        targetParticipants = "40",
        category = "Music",
        activityType = "Festive"
    )

    private fun checkForm(form: EventFormInput): EventFormResult {
        val result = EventForm.read(form)
        val errors = linkedMapOf<EventField, String>()
        errors.putAll(result.errors)
        val ruleErrors = EventRules.validate(result.event)
        for (field in ruleErrors.keys) {
            val message = ruleErrors[field]
            if (message != null && !errors.containsKey(field)) {
                errors[field] = message
            }
        }
        return EventFormResult(errors, result.event)
    }

    private fun validEvent(form: EventFormInput): NewEvent {
        val result = checkForm(form)
        if (result.errors.isNotEmpty()) {
            throw AssertionError("The form should be valid: " + result.errors)
        }
        return result.event
    }

    @Test
    fun blankPriceIsFree() {
        val event = validEvent(validForm)

        assertEquals(0.0, event.memberPrice, 0.0)
        assertEquals(0.0, event.nonMemberPrice, 0.0)
    }

    @Test
    fun pricesAreSaved() {
        val form = validForm.copy(memberPrice = "30", nonMemberPrice = "35.50")

        val event = validEvent(form)

        assertEquals(30.0, event.memberPrice, 0.0)
        assertEquals(35.5, event.nonMemberPrice, 0.0)
    }

    @Test
    fun badPrices() {
        assertNull(EventForm.parsePrice("abc"))
        for (price in listOf("-5", "abc", "12.345", "10001")) {
            val errors = checkForm(validForm.copy(memberPrice = price)).errors
            assertTrue("$price should be rejected", errors.containsKey(EventField.MEMBER_PRICE))
        }
    }

    @Test
    fun targetParticipants() {
        val withoutTarget = checkForm(validForm.copy(targetParticipants = ""))
        assertTrue(withoutTarget.errors.containsKey(EventField.TARGET_PARTICIPANTS))

        val withTarget = validEvent(validForm.copy(targetParticipants = "40"))
        assertEquals(40, withTarget.targetParticipants)

        val bad = checkForm(validForm.copy(targetParticipants = "lots"))
        assertTrue(bad.errors.containsKey(EventField.TARGET_PARTICIPANTS))
    }

    @Test
    fun linkOrBase64() {
        assertTrue(EventMedia.isLink("https://example.com/poster.jpg"))
        assertTrue(EventMedia.isLink("http://example.com/poster.jpg"))
        assertFalse(EventMedia.isLink("/9j/4AAQSkZJRgABAQ"))
    }

    @Test
    fun shrinkPoster() {
        assertEquals(Pair(1080, 1527), EventMedia.scaledSize(2480, 3508))
    }

    @Test
    fun smallPosterStaysSame() {
        assertEquals(Pair(500, 700), EventMedia.scaledSize(500, 700))
    }

    @Test
    fun defaultAlternativeText() {
        var savedPosters: List<PosterUpload> = emptyList()
        var savedFiles: List<FileUpload> = emptyList()
        val viewModel = CreateEventViewModel { _, _: NewEvent, posters, files, _ ->
            savedPosters = posters
            savedFiles = files
        }

        viewModel.onFieldChange(EventField.TITLE, "Music Festival")
        viewModel.onFieldChange(EventField.VENUE, "F3C09")
        viewModel.onFieldChange(EventField.START, "2099-10-24 12:00")
        viewModel.onFieldChange(EventField.END, "2099-10-24 13:00")
        viewModel.onFieldChange(EventField.CAPACITY, "50")
        viewModel.onFieldChange(EventField.POINTS, "2")
        viewModel.onFieldChange(EventField.TARGET_PARTICIPANTS, "40")
        viewModel.onFieldChange(EventField.CATEGORY, "Music")
        viewModel.onFieldChange(EventField.ACTIVITY_TYPE, "Festive")
        viewModel.setEpf(FileUpload(FileKind.EPF, "epf.pdf", "application/pdf", ""))
        viewModel.addPoster(PosterUpload(image = "abc"))
        viewModel.addPoster(PosterUpload(image = "def", alternativeText = "Jazz band on stage"))
        viewModel.submit("club1")

        assertEquals("Music Festival poster", savedPosters[0].alternativeText)
        assertEquals("Jazz band on stage", savedPosters[1].alternativeText)
        assertEquals(1, savedFiles.size)
    }

    @Test
    fun maxPosters() {
        val viewModel = CreateEventViewModel { _, _, _, _, _ -> }

        for (number in 1..6) {
            viewModel.addPoster(PosterUpload(image = "poster$number"))
        }

        assertEquals(EventMedia.MAXIMUM_POSTERS, viewModel.ui.posters.size)
    }

    private val utc = TimeZone.getTimeZone("UTC")

    private fun session(start: String, end: String): EventSession {
        val startTime = EventForm.parseDateTime(start, utc)
        val endTime = EventForm.parseDateTime(end, utc)
        if (startTime == null || endTime == null) {
            throw AssertionError("Bad test date")
        }
        return EventSession(startTime, endTime)
    }

    @Test
    fun datesSetStartAndEnd() {
        val first = session("2099-10-24 12:00", "2099-10-24 13:00")
        val second = session("2099-10-25 21:00", "2099-10-25 23:00")
        val form = validForm.copy(start = "", end = "", sessions = listOf(first, second))

        val event = validEvent(form)

        assertEquals(first.startTime, event.startTime)
        assertEquals(second.endTime, event.endTime)
        assertEquals(2, event.sessions.size)
    }

    @Test
    fun noDates() {
        val form = validForm.copy(start = "", end = "", sessions = emptyList())

        val result = checkForm(form)

        assertEquals("Add at least one date", result.errors[EventField.START])
    }

    @Test
    fun dateEndBeforeStart() {
        val backwards = session("2099-10-24 13:00", "2099-10-24 12:00")
        val form = validForm.copy(start = "", end = "", sessions = listOf(backwards))

        val result = checkForm(form)

        assertTrue(result.errors.containsKey(EventField.END))
    }

    @Test
    fun dateRangeMakesOneDatePerDay() {
        val firstDay = EventForm.parseDateTime("2099-10-24 00:00", utc)
        val lastDay = EventForm.parseDateTime("2099-10-26 00:00", utc)
        if (firstDay == null || lastDay == null) {
            throw AssertionError("Bad test date")
        }

        val sessions = EventForm.makeSessions(firstDay, lastDay, 21 * 60, 23 * 60, utc)

        assertEquals(3, sessions.size)
        assertEquals("24/10/2099, 21:00 - 23:00", EventForm.formatSession(sessions[0], utc))
        assertEquals("26/10/2099, 21:00 - 23:00", EventForm.formatSession(sessions[2], utc))
    }

    @Test
    fun datesAreSorted() {
        val viewModel = CreateEventViewModel { _, _, _, _, _ -> }
        val later = session("2099-10-25 21:00", "2099-10-25 23:00")
        val earlier = session("2099-10-24 12:00", "2099-10-24 13:00")

        viewModel.addSessions(listOf(later))
        viewModel.addSessions(listOf(earlier, later))

        assertEquals(2, viewModel.ui.form.sessions.size)
        assertEquals(earlier, viewModel.ui.form.sessions[0])
    }

    @Test
    fun blankPointsMeansNoScpd() {
        val event = validEvent(validForm.copy(points = ""))

        assertEquals(0, event.pointsRequested)
    }

    @Test
    fun badPoints() {
        val result = checkForm(validForm.copy(points = "-1"))

        assertTrue(result.errors.containsKey(EventField.POINTS))
    }

    @Test
    fun categoryAndActivityTypeRequired() {
        val result = checkForm(validForm.copy(category = "", activityType = ""))

        assertTrue(result.errors.containsKey(EventField.CATEGORY))
        assertTrue(result.errors.containsKey(EventField.ACTIVITY_TYPE))
    }

    @Test
    fun epfRequired() {
        var calls = 0
        val viewModel = CreateEventViewModel { _, _, _, _, _ -> calls = calls + 1 }

        viewModel.onFieldChange(EventField.TITLE, "Music Festival")
        viewModel.onFieldChange(EventField.VENUE, "F3C09")
        viewModel.onFieldChange(EventField.START, "2099-10-24 12:00")
        viewModel.onFieldChange(EventField.END, "2099-10-24 13:00")
        viewModel.onFieldChange(EventField.CAPACITY, "50")
        viewModel.onFieldChange(EventField.TARGET_PARTICIPANTS, "40")
        viewModel.onFieldChange(EventField.CATEGORY, "Music")
        viewModel.onFieldChange(EventField.ACTIVITY_TYPE, "Festive")
        viewModel.submit("club1")

        assertEquals(0, calls)
        assertEquals("Upload the EPF", viewModel.ui.errors[EventField.EPF])
    }

    @Test
    fun oneDateWithTimes() {
        val day = EventForm.parseDateTime("2099-10-24 00:00", utc)
        if (day == null) {
            throw AssertionError("Bad test date")
        }

        val session = EventForm.makeSession(day, 12 * 60, day, 13 * 60, utc)
        if (session == null) {
            throw AssertionError("Session should be made")
        }

        assertEquals("24/10/2099, 12:00 - 13:00", EventForm.formatSession(session, utc))
    }

    @Test
    fun dateOverMidnight() {
        val firstDay = EventForm.parseDateTime("2099-10-24 00:00", utc)
        val nextDay = EventForm.parseDateTime("2099-10-25 00:00", utc)
        if (firstDay == null || nextDay == null) {
            throw AssertionError("Bad test date")
        }

        val session = EventForm.makeSession(firstDay, 21 * 60, nextDay, 1 * 60, utc)
        if (session == null) {
            throw AssertionError("Session should be made")
        }

        assertEquals("24/10/2099, 21:00 - 25/10/2099, 01:00", EventForm.formatSession(session, utc))
    }

    @Test
    fun twoDigits() {
        assertEquals("07", EventForm.twoDigits(7))
        assertEquals("23", EventForm.twoDigits(23))
    }

    private val epfFile = FileUpload(FileKind.EPF, "epf.pdf", "application/pdf", "")

    @Test
    fun rulesAcceptValidEvent() {
        val event = validEvent(validForm)

        assertTrue(EventRules.validate(event).isEmpty())
        assertTrue(EventRules.hasEpf(listOf(epfFile)))
    }

    @Test
    fun rulesNeedEpf() {
        assertFalse(EventRules.hasEpf(emptyList()))
    }

    @Test
    fun rulesCheckEveryDate() {
        val good = session("2099-10-24 12:00", "2099-10-24 13:00")
        val backwards = session("2099-10-25 13:00", "2099-10-25 12:00")
        val event = validEvent(validForm).copy(sessions = listOf(good, backwards))

        assertEquals(
            "Each end time must be after its start time",
            EventRules.validate(event)[EventField.END]
        )
    }

    @Test
    fun rulesNeedCategory() {
        val event = validEvent(validForm).copy(category = "")

        assertEquals("Pick a category", EventRules.validate(event)[EventField.CATEGORY])
    }

    @Test
    fun bigTargetAllowed() {
        val event = validEvent(validForm.copy(targetParticipants = "5000"))

        assertEquals(5000, event.targetParticipants)
        assertTrue(EventRules.validate(event).isEmpty())
    }
}

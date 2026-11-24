package com.example.myapplication.ui.events

import com.example.myapplication.events.EventField
import com.example.myapplication.events.EventForm
import com.example.myapplication.events.EventSession
import com.example.myapplication.events.FileKind
import com.example.myapplication.events.FileUpload
import com.example.myapplication.events.PosterUpload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateEventViewModelTest {

    private class FakeCreate {
        var calls = 0
        var clubId: String? = null
        var title: String? = null
        var capacity: Int? = null
        var posters: List<PosterUpload> = emptyList()
        var sessions = 0
        var callback: ((Boolean, String?) -> Unit)? = null

        val call: CreateEventCall = { club, event, posters, _, result ->
            calls++
            this.clubId = club
            this.title = event.title
            this.capacity = event.capacity
            this.posters = posters
            this.sessions = event.sessions.size
            callback = result
        }
    }

    private val fake = FakeCreate()
    private val vm = CreateEventViewModel(fake.call)

    // Far-future dates so the test does not depend on today's date.
    private fun date(start: String, end: String): EventSession {
        val startTime = EventForm.parseDateTime(start)!!
        val endTime = EventForm.parseDateTime(end)!!
        check(startTime > System.currentTimeMillis())
        return EventSession(startTime, endTime)
    }

    private fun fillValidForm() {
        vm.onFieldChange(EventField.TITLE, "Hackathon")
        vm.onFieldChange(EventField.VENUE, "Hall A")
        vm.addSessions(listOf(date("2099-12-05 10:00", "2099-12-05 18:00")))
        vm.onFieldChange(EventField.CAPACITY, "50")
        vm.onFieldChange(EventField.POINTS, "5")
        vm.onFieldChange(EventField.TARGET_PARTICIPANTS, "40")
        vm.onFieldChange(EventField.CATEGORY, "Technology")
        vm.onFieldChange(EventField.ACTIVITY_TYPE, "Competition")
        vm.setEpf(FileUpload(FileKind.EPF, "epf.pdf", "application/pdf", ""))
    }

    @Test fun invalidForm_showsErrorsAndNeverCallsTheServer() {
        vm.submit("club1")
        assertEquals(0, fake.calls)
        assertTrue(vm.ui.errors.containsKey(EventField.TITLE))
        assertFalse(vm.ui.isSubmitting)
    }

    @Test fun validForm_callsCreateWithTheClubId() {
        fillValidForm()
        vm.submit("club1")
        assertEquals(1, fake.calls)
        assertEquals("club1", fake.clubId)
        assertEquals("Hackathon", fake.title)
        assertEquals(50, fake.capacity)
        assertTrue(vm.ui.isSubmitting)
    }

    @Test fun success_clearsTheFormAndShowsSubmitted() {
        fillValidForm()
        vm.submit("club1")
        fake.callback!!.invoke(true, "event123")
        assertTrue(vm.ui.submitted)
        assertFalse(vm.ui.isSubmitting)
        assertEquals("", vm.ui.form.title)
    }

    @Test fun failure_keepsTheFormAndShowsTheMessage() {
        fillValidForm()
        vm.submit("club1")
        fake.callback!!.invoke(false, "You are not a committee member of this club")
        assertEquals("You are not a committee member of this club", vm.ui.message)
        assertEquals("Hackathon", vm.ui.form.title)
        assertFalse(vm.ui.isSubmitting)
        assertFalse(vm.ui.submitted)
    }

    @Test fun failureWithoutAMessage_usesADefault() {
        fillValidForm()
        vm.submit("club1")
        fake.callback!!.invoke(false, null)
        assertNotNull(vm.ui.message)
    }

    @Test fun secondTapWhileSubmitting_isIgnored() {
        fillValidForm()
        vm.submit("club1")
        vm.submit("club1")
        assertEquals(1, fake.calls)
    }

    @Test fun editingAField_clearsOnlyThatFieldsError() {
        vm.submit("club1")
        assertTrue(vm.ui.errors.containsKey(EventField.TITLE))
        assertTrue(vm.ui.errors.containsKey(EventField.VENUE))
        vm.onFieldChange(EventField.TITLE, "Chess")
        assertNull(vm.ui.errors[EventField.TITLE])
        assertTrue(vm.ui.errors.containsKey(EventField.VENUE))
    }

    @Test fun noDate_showsDateError() {
        fillValidForm()
        vm.removeSession(0)
        vm.submit("club1")
        assertEquals(0, fake.calls)
        assertEquals("Add at least one date", vm.ui.errors[EventField.START])
    }

    @Test fun missingEpf_blocksSubmit() {
        fillValidForm()
        vm.setEpf(null)
        vm.submit("club1")
        assertEquals(0, fake.calls)
        assertEquals("Upload the EPF", vm.ui.errors[EventField.EPF])
    }

    @Test fun datesAreSaved() {
        fillValidForm()
        vm.addSessions(listOf(date("2099-12-06 10:00", "2099-12-06 12:00")))
        vm.submit("club1")
        assertEquals(1, fake.calls)
        assertEquals(2, fake.sessions)
    }

    @Test fun textInANumberBox_showsError() {
        fillValidForm()
        vm.onFieldChange(EventField.CAPACITY, "lots")
        vm.submit("club1")
        assertEquals(0, fake.calls)
        assertTrue(vm.ui.errors.containsKey(EventField.CAPACITY))
    }
}

package com.example.myapplication.ui.events

import com.example.myapplication.events.EventField
import com.example.myapplication.events.EventForm
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
        var callback: ((Boolean, String?) -> Unit)? = null

        val call: CreateEventCall = { club, title, _, _, _, _, capacity, _, result ->
            calls++
            this.clubId = club
            this.title = title
            this.capacity = capacity
            callback = result
        }
    }

    private val fake = FakeCreate()
    private val vm = CreateEventViewModel(fake.call)

    private fun fillValidForm() {
        // Far-future dates so the test does not depend on today's date.
        val start = EventForm.parseDateTime("2099-12-05 10:00")!!
        check(start > System.currentTimeMillis())
        vm.onFieldChange(EventField.TITLE, "Hackathon")
        vm.onFieldChange(EventField.VENUE, "Hall A")
        vm.onFieldChange(EventField.START, "2099-12-05 10:00")
        vm.onFieldChange(EventField.END, "2099-12-05 18:00")
        vm.onFieldChange(EventField.CAPACITY, "50")
        vm.onFieldChange(EventField.POINTS, "5")
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
}
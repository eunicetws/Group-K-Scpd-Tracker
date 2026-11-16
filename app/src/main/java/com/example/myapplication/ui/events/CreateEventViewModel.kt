package com.example.myapplication.ui.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.myapplication.events.EventField
import com.example.myapplication.events.EventForm
import com.example.myapplication.events.EventFormInput
import com.example.myapplication.events.EventManager

data class CreateEventUiState(
    val form: EventFormInput = EventFormInput(),
    val errors: Map<EventField, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val message: String? = null,
    val submitted: Boolean = false
)

typealias CreateEventCall = (
    clubId: String, title: String, description: String, venue: String,
    startTime: Long, endTime: Long, capacity: Int, pointsRequested: Int,
    result: (Boolean, String?) -> Unit
) -> Unit

class CreateEventViewModel(
    private val create: CreateEventCall = EventManager::createEvent
) : ViewModel() {

    var ui by mutableStateOf(CreateEventUiState())
        private set

    fun onFieldChange(field: EventField, value: String) {
        val form = ui.form
        val newForm = when (field) {
            EventField.TITLE -> form.copy(title = value)
            EventField.DESCRIPTION -> form.copy(description = value)
            EventField.VENUE -> form.copy(venue = value)
            EventField.START -> form.copy(start = value)
            EventField.END -> form.copy(end = value)
            EventField.CAPACITY -> form.copy(capacity = value)
            EventField.POINTS -> form.copy(points = value)
        }
        // Editing a field clears its own error and any old status message.
        ui = ui.copy(form = newForm, errors = ui.errors - field, message = null, submitted = false)
    }

    fun submit(clubId: String) {
        if (ui.isSubmitting) return

        val result = EventForm.validate(ui.form)
        val event = result.event
        if (event == null) {
            ui = ui.copy(errors = result.errors, message = null, submitted = false)
            return
        }

        ui = ui.copy(errors = emptyMap(), isSubmitting = true, message = null, submitted = false)
        create(
            clubId, event.title, event.description, event.venue,
            event.startTime, event.endTime, event.capacity, event.pointsRequested
        ) { success, info ->
            ui = if (success) {
                CreateEventUiState(submitted = true)
            } else {
                ui.copy(isSubmitting = false, message = info ?: "Could not submit the event")
            }
        }
    }
}
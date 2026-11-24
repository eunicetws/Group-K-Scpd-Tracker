package com.example.myapplication.ui.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.myapplication.events.EventField
import com.example.myapplication.events.EventForm
import com.example.myapplication.events.EventFormInput
import com.example.myapplication.events.EventManager
import com.example.myapplication.events.EventMedia
import com.example.myapplication.events.EventRules
import com.example.myapplication.events.EventSession
import com.example.myapplication.events.FileUpload
import com.example.myapplication.events.NewEvent
import com.example.myapplication.events.PosterUpload

data class CreateEventUiState(
    val form: EventFormInput = EventFormInput(),
    val errors: Map<EventField, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val message: String? = null,
    val submitted: Boolean = false,
    val posters: List<PosterUpload> = emptyList(),
    val epf: FileUpload? = null,
    val attachments: List<FileUpload> = emptyList()
)

typealias CreateEventCall = (
    clubId: String,
    event: NewEvent,
    posters: List<PosterUpload>,
    files: List<FileUpload>,
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
            EventField.MEMBER_PRICE -> form.copy(memberPrice = value)
            EventField.NON_MEMBER_PRICE -> form.copy(nonMemberPrice = value)
            EventField.TARGET_PARTICIPANTS -> form.copy(targetParticipants = value)
            EventField.CATEGORY -> form.copy(category = value)
            EventField.ACTIVITY_TYPE -> form.copy(activityType = value)
            EventField.EPF -> form
        }
        // Editing a field clears its own error and any old status message.
        ui = ui.copy(form = newForm, errors = ui.errors - field, message = null, submitted = false)
    }

    fun showMessage(message: String) {
        ui = ui.copy(message = message, submitted = false)
    }

    fun addSessions(newSessions: List<EventSession>) {
        val sessions = ui.form.sessions.toMutableList()
        for (newSession in newSessions) {
            if (sessions.contains(newSession)) {
                continue
            }
            var position = 0
            while (position < sessions.size && sessions[position].startTime <= newSession.startTime) {
                position += 1
            }
            sessions.add(position, newSession)
        }
        val errors = ui.errors - EventField.START - EventField.END
        ui = ui.copy(form = ui.form.copy(sessions = sessions), errors = errors, message = null, submitted = false)
    }

    fun removeSession(index: Int) {
        if (index < 0 || index >= ui.form.sessions.size) {
            return
        }
        val sessions = ui.form.sessions.toMutableList()
        sessions.removeAt(index)
        ui = ui.copy(form = ui.form.copy(sessions = sessions))
    }

    fun addPoster(poster: PosterUpload) {
        if (ui.posters.size >= EventMedia.MAXIMUM_POSTERS) {
            showMessage("You can add up to ${EventMedia.MAXIMUM_POSTERS} posters")
            return
        }
        val posters = ui.posters.toMutableList()
        posters.add(poster)
        ui = ui.copy(posters = posters, message = null, submitted = false)
    }

    fun removePoster(index: Int) {
        if (index < 0 || index >= ui.posters.size) {
            return
        }
        val posters = ui.posters.toMutableList()
        posters.removeAt(index)
        ui = ui.copy(posters = posters)
    }

    fun setPosterAlternativeText(index: Int, alternativeText: String) {
        if (index < 0 || index >= ui.posters.size) {
            return
        }
        val posters = ui.posters.toMutableList()
        posters[index] = posters[index].copy(alternativeText = alternativeText)
        ui = ui.copy(posters = posters)
    }

    fun setEpf(file: FileUpload?) {
        ui = ui.copy(epf = file, errors = ui.errors - EventField.EPF, message = null, submitted = false)
    }

    fun addAttachment(file: FileUpload) {
        val attachments = ui.attachments.toMutableList()
        attachments.add(file)
        ui = ui.copy(attachments = attachments, message = null, submitted = false)
    }

    fun removeAttachment(index: Int) {
        if (index < 0 || index >= ui.attachments.size) {
            return
        }
        val attachments = ui.attachments.toMutableList()
        attachments.removeAt(index)
        ui = ui.copy(attachments = attachments)
    }

    fun submit(clubId: String) {
        if (ui.isSubmitting) return

        val result = EventForm.read(ui.form)
        val event = result.event
        val errors = linkedMapOf<EventField, String>()
        errors.putAll(result.errors)
        val ruleErrors = EventRules.validate(event)
        for (field in ruleErrors.keys) {
            val message = ruleErrors[field]
            if (message != null && !errors.containsKey(field)) {
                errors[field] = message
            }
        }
        if (ui.epf == null) {
            errors[EventField.EPF] = "Upload the EPF"
        }
        if (errors.isNotEmpty()) {
            ui = ui.copy(errors = errors, message = null, submitted = false)
            return
        }

        val posters = mutableListOf<PosterUpload>()
        for (index in ui.posters.indices) {
            val poster = ui.posters[index]
            var alternativeText = poster.alternativeText.trim()
            if (alternativeText.isEmpty()) {
                alternativeText = EventMedia.defaultAlternativeText(event.title, index + 1)
            }
            posters.add(poster.copy(alternativeText = alternativeText))
        }

        val files = mutableListOf<FileUpload>()
        val epf = ui.epf
        if (epf != null) {
            files.add(epf)
        }
        files.addAll(ui.attachments)

        ui = ui.copy(errors = emptyMap(), isSubmitting = true, message = null, submitted = false)
        create(clubId, event, posters, files) { success, info ->
            ui = if (success) {
                CreateEventUiState(submitted = true)
            } else {
                ui.copy(isSubmitting = false, message = info ?: "Could not submit the event")
            }
        }
    }
}

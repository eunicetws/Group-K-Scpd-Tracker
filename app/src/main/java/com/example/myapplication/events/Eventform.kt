package com.example.myapplication.events

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

enum class EventField { TITLE, DESCRIPTION, VENUE, START, END, CAPACITY, POINTS }

// Everything the user typed
data class EventFormInput(
    val title: String = "",
    val description: String = "",
    val venue: String = "",
    val start: String = "",
    val end: String = "",
    val capacity: String = "",
    val points: String = ""
)

// Cleaned-up values to send to Eventmanager
data class NewEvent(
    val title: String,
    val description: String,
    val venue: String,
    val startTime: Long,
    val endTime: Long,
    val capacity: Int,
    val pointsRequested: Int
)

// errors is empty exactly when event is not null.
data class EventFormResult(val errors: Map<EventField, String>, val event: NewEvent?)

object EventForm {

    const val DATE_FORMAT = "yyyy-MM-dd HH:mm"

    // Returns epoch milliseconds, or null if the text is not exactly DATE_FORMAT
    fun parseDateTime(text: String, zone: TimeZone = TimeZone.getDefault()): Long? {
        val trimmed = text.trim()
        val format = SimpleDateFormat(DATE_FORMAT, Locale.US).apply {
            isLenient = false
            timeZone = zone
        }
        val position = ParsePosition(0)
        val date = format.parse(trimmed, position)
        return if (date == null || position.index != trimmed.length) null else date.time
    }

    // Checks every field so the screen can highlight all the invalid ones at once.
    fun validate(
        input: EventFormInput,
        now: Long = System.currentTimeMillis(),
        zone: TimeZone = TimeZone.getDefault()
    ): EventFormResult {
        val errors = linkedMapOf<EventField, String>()

        if (input.title.isBlank()) errors[EventField.TITLE] = "Title is required"
        if (input.venue.isBlank()) errors[EventField.VENUE] = "Venue is required"

        val start = parseDateTime(input.start, zone)
        val end = parseDateTime(input.end, zone)
        when {
            start == null -> errors[EventField.START] = "Enter the start as $DATE_FORMAT"
            start <= now -> errors[EventField.START] = "Start time must be in the future"
        }
        when {
            end == null -> errors[EventField.END] = "Enter the end as $DATE_FORMAT"
            start != null && end <= start -> errors[EventField.END] = "End time must be after the start time"
        }

        val capacity = input.capacity.trim().toIntOrNull()
        if (capacity == null || capacity !in 1..EventRules.MAX_CAPACITY) {
            errors[EventField.CAPACITY] = "Capacity must be a whole number from 1 to ${EventRules.MAX_CAPACITY}"
        }
        val points = input.points.trim().toIntOrNull()
        if (points == null || points !in 1..EventRules.MAX_POINTS) {
            errors[EventField.POINTS] = "Points must be a whole number from 1 to ${EventRules.MAX_POINTS}"
        }

        if (errors.isNotEmpty()) return EventFormResult(errors, null)

        return EventFormResult(
            errors,
            NewEvent(
                title = input.title.trim(),
                description = input.description.trim(),
                venue = input.venue.trim(),
                startTime = start!!,
                endTime = end!!,
                capacity = capacity!!,
                pointsRequested = points!!
            )
        )
    }
}
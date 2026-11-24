package com.example.myapplication.events

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class EventField {
    TITLE, DESCRIPTION, VENUE, START, END, CAPACITY, POINTS,
    MEMBER_PRICE, NON_MEMBER_PRICE, TARGET_PARTICIPANTS, CATEGORY, ACTIVITY_TYPE, EPF
}

// Input
data class EventFormInput(
    val title: String = "",
    val description: String = "",
    val venue: String = "",
    val start: String = "",
    val end: String = "",
    val sessions: List<EventSession> = emptyList(),
    val capacity: String = "",
    val points: String = "",
    val memberPrice: String = "",
    val nonMemberPrice: String = "",
    val targetParticipants: String = "",
    val category: String = "",
    val activityType: String = ""
)

// Output
data class NewEvent(
    val title: String,
    val description: String,
    val venue: String,
    val startTime: Long,
    val endTime: Long,
    val capacity: Int,
    val pointsRequested: Int,
    val sessions: List<EventSession> = emptyList(),
    val memberPrice: Double = 0.0,
    val nonMemberPrice: Double = 0.0,
    val targetParticipants: Int = 0,
    val category: String = "",
    val activityType: String = ""
)

data class EventFormResult(val errors: Map<EventField, String>, val event: NewEvent)

object EventForm {

    const val DATE_FORMAT = "yyyy-MM-dd HH:mm"

    // Date as milliseconds
    fun parseDateTime(text: String, zone: TimeZone = TimeZone.getDefault()): Long? {
        val trimmed = text.trim()
        val format = SimpleDateFormat(DATE_FORMAT, Locale.US).apply {
            isLenient = false
            timeZone = zone
        }
        val position = ParsePosition(0) // prevent extra chac at the back
        val date = format.parse(trimmed, position)
        return if (date == null || position.index != trimmed.length) null else date.time
    }

    fun read(
        input: EventFormInput,
        zone: TimeZone = TimeZone.getDefault()
    ): EventFormResult {
        val errors = linkedMapOf<EventField, String>()

        // Date
        var start = 0L
        var end = 0L
        var sessions = input.sessions
        if (input.sessions.isNotEmpty()) {
            start = input.sessions[0].startTime
            end = input.sessions[0].endTime
            for ((startTime, endTime) in input.sessions) {
                if (startTime < start) {
                    start = startTime
                }
                if (endTime > end) {
                    end = endTime
                }
            }
        } else if (input.start.isNotBlank() || input.end.isNotBlank()) {
            val typedStart = parseDateTime(input.start, zone)
            val typedEnd = parseDateTime(input.end, zone)
            if (typedStart != null && typedEnd != null) {
                start = typedStart
                end = typedEnd
                sessions = listOf(EventSession(typedStart, typedEnd))
            }
        }

        // capacity
        var capacity = 0
        if (input.capacity.isNotBlank()) {
            val capacityResult = input.capacity.trim().toIntOrNull()
            if (capacityResult == null) {
                errors[EventField.CAPACITY] = EventRules.CAPACITY_NOT_NUMBER
            } else {
                capacity = capacityResult
            }
        }

        // scpd points
        var points = 0
        if (input.points.isNotBlank()) {
            val pointsResult = input.points.trim().toIntOrNull()
            if (pointsResult == null) {
                errors[EventField.POINTS] = EventRules.POINTS_NOT_NUMBER
            } else {
                points = pointsResult
            }
        }

        var memberPrice = 0.0
        val memberPriceResult = parsePrice(input.memberPrice)
        if (memberPriceResult == null) {
            errors[EventField.MEMBER_PRICE] = EventRules.PRICE_NOT_NUMBER
        } else {
            memberPrice = memberPriceResult
        }

        var nonMemberPrice = 0.0
        val nonMemberPriceResult = parsePrice(input.nonMemberPrice)
        if (nonMemberPriceResult == null) {
            errors[EventField.NON_MEMBER_PRICE] = EventRules.PRICE_NOT_NUMBER
        } else {
            nonMemberPrice = nonMemberPriceResult
        }

        var targetParticipants = 0
        if (input.targetParticipants.isNotBlank()) {
            val targetResult = input.targetParticipants.trim().toIntOrNull()
            if (targetResult == null) {
                errors[EventField.TARGET_PARTICIPANTS] = EventRules.TARGET_NOT_NUMBER
            } else {
                targetParticipants = targetResult
            }
        }

        val event = NewEvent(
            title = input.title.trim(),
            description = input.description.trim(),
            venue = input.venue.trim(),
            startTime = start,
            endTime = end,
            capacity = capacity,
            pointsRequested = points,
            sessions = sessions,
            memberPrice = memberPrice,
            nonMemberPrice = nonMemberPrice,
            targetParticipants = targetParticipants,
            category = input.category.trim(),
            activityType = input.activityType.trim()
        )

        return EventFormResult(errors, event)
    }

    const val MAXIMUM_DAYS = 31
    private const val ONE_DAY_MILLISECONDS = 24 * 60 * 60 * 1000L

    fun makeSessions(
        firstDayUtc: Long,
        lastDayUtc: Long,
        startMinutes: Int,
        endMinutes: Int,
        zone: TimeZone = TimeZone.getDefault()

    ): List<EventSession> {
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dayFormat.timeZone = TimeZone.getTimeZone("UTC")
        val startText = minutesToTime(startMinutes)
        val endText = minutesToTime(endMinutes)

        val sessions = mutableListOf<EventSession>()
        var day = firstDayUtc
        while (day <= lastDayUtc && sessions.size < MAXIMUM_DAYS) {
            val dayText = dayFormat.format(Date(day))
            val start = parseDateTime("$dayText $startText", zone)
            val end = parseDateTime("$dayText $endText", zone)
            if (start != null && end != null) {
                sessions.add(EventSession(start, end))
            }
            day += ONE_DAY_MILLISECONDS
        }
        return sessions
    }

    fun formatSession(session: EventSession, zone: TimeZone = TimeZone.getDefault()): String {
        val dayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        dayFormat.timeZone = zone
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
        timeFormat.timeZone = zone
        val startDay = dayFormat.format(Date(session.startTime))
        val endDay = dayFormat.format(Date(session.endTime))
        val start = timeFormat.format(Date(session.startTime))
        val end = timeFormat.format(Date(session.endTime))
        if (startDay == endDay) {
            return "$startDay, $start - $end"
        }
        return "$startDay, $start - $endDay, $end"
    }

    fun makeSession(
        startDayUtc: Long,
        startMinutes: Int,
        endDayUtc: Long,
        endMinutes: Int,
        zone: TimeZone = TimeZone.getDefault()
    ): EventSession? {
        val start = parseDateTime(formatDay(startDayUtc, "yyyy-MM-dd") + " " + minutesToTime(startMinutes), zone)
        val end = parseDateTime(formatDay(endDayUtc, "yyyy-MM-dd") + " " + minutesToTime(endMinutes), zone)
        if (start == null || end == null) {
            return null
        }
        return EventSession(start, end)
    }

    fun formatDay(dayUtc: Long, pattern: String = "dd/MM/yyyy"): String {
        val dayFormat = SimpleDateFormat(pattern, Locale.US)
        dayFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dayFormat.format(Date(dayUtc))
    }

    fun twoDigits(number: Int): String {
        if (number < 10) {
            return "0$number"
        }
        return number.toString()
    }

    private fun minutesToTime(minutes: Int): String {
        val hour = minutes / 60
        val minute = minutes % 60
        return String.format(Locale.US, "%02d:%02d", hour, minute)
    }


    fun parsePrice(text: String): Double? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return 0.0
        }
        return trimmed.toDoubleOrNull()
    }
}
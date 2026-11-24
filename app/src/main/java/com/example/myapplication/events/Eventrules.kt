package com.example.myapplication.events

object EventRules {

    // Assumed limits, adjust to whatever the team writes in its report.
    const val MAX_CAPACITY = 1000
    const val MAX_POINTS = 100
    const val MAX_PRICE = 10000.0

    // Shown when the typed text is not a number at all, for example "abc"
    const val CAPACITY_NOT_NUMBER = "Capacity must be a whole number"
    const val POINTS_NOT_NUMBER = "Points must be a whole number"
    const val TARGET_NOT_NUMBER = "Target must be a whole number"
    const val PRICE_NOT_NUMBER = "Price must be a number, for example 30 or 12.50"

    fun validate(event: NewEvent, now: Long = System.currentTimeMillis()): Map<EventField, String> {
        val errors = linkedMapOf<EventField, String>()

        if (event.title.isBlank()) addError(errors, EventField.TITLE, "Title is required")
        if (event.venue.isBlank()) addError(errors, EventField.VENUE, "Venue is required")

        if (event.sessions.isEmpty()) addError(errors, EventField.START, "Add at least one date")
        if (event.startTime <= 0L || event.endTime <= 0L) {
            addError(errors, EventField.START, "Start and end time are required")
        }
        if (event.startTime <= now) addError(errors, EventField.START, "Start time must be in the future")
        if (event.startTime > 0L && event.endTime <= event.startTime) {
            addError(errors, EventField.END, "End time must be after the start time")
        }
        for (session in event.sessions) {
            if (session.startTime <= now) {
                addError(errors, EventField.START, "Every date must be in the future")
            }
            if (session.endTime <= session.startTime) {
                addError(errors, EventField.END, "Each end time must be after its start time")
            }
        }

        if (event.capacity !in 1..MAX_CAPACITY) {
            addError(errors, EventField.CAPACITY, "Capacity must be between 1 and $MAX_CAPACITY")
        }
        if (event.pointsRequested !in 0..MAX_POINTS) {
            addError(errors, EventField.POINTS, "Points requested must be between 0 and $MAX_POINTS")
        }
        if (event.targetParticipants < 1) {
            addError(errors, EventField.TARGET_PARTICIPANTS, "Target participants must be at least 1")
        }
        if (event.category.isBlank()) addError(errors, EventField.CATEGORY, "Pick a category")
        if (event.activityType.isBlank()) addError(errors, EventField.ACTIVITY_TYPE, "Pick an activity type")
        if (event.memberPrice !in 0.0..MAX_PRICE) {
            addError(errors, EventField.MEMBER_PRICE, "Member price must be between 0 and $MAX_PRICE")
        }
        if (!hasAtMostTwoDecimals(event.memberPrice)) {
            addError(errors, EventField.MEMBER_PRICE, "Member price can have at most 2 decimal places")
        }
        if (event.nonMemberPrice !in 0.0..MAX_PRICE) {
            addError(errors, EventField.NON_MEMBER_PRICE, "Non-member price must be between 0 and $MAX_PRICE")
        }
        if (!hasAtMostTwoDecimals(event.nonMemberPrice)) {
            addError(errors, EventField.NON_MEMBER_PRICE, "Non-member price can have at most 2 decimal places")
        }
        return errors
    }

    // Prices are in RM and sen, so 12.50 is fine but 12.345 is not
    private fun hasAtMostTwoDecimals(price: Double): Boolean {
        val sen = price * 100
        val difference = Math.abs(sen - Math.round(sen))
        return difference < 0.000001
    }

    // Keeps the first error for each field
    private fun addError(errors: LinkedHashMap<EventField, String>, field: EventField, message: String) {
        if (!errors.containsKey(field)) {
            errors[field] = message
        }
    }

    fun hasEpf(files: List<FileUpload>): Boolean {
        for (file in files) {
            if (file.kind == FileKind.EPF) {
                return true
            }
        }
        return false
    }

    // Once an event is approved it can never be edited.
    fun canEdit(status: EventStatus): Boolean = status == EventStatus.PENDING

    fun canCancel(status: EventStatus): Boolean =
        status == EventStatus.PENDING || status == EventStatus.APPROVED

    // Marketplace search by title, venue or description (case-insensitive).
    fun search(events: List<Event>, query: String): List<Event> {
        val q = query.trim()
        if (q.isEmpty()) return events
        return events.filter {
            it.title.contains(q, ignoreCase = true) ||
                    it.venue.contains(q, ignoreCase = true) ||
                    it.description.contains(q, ignoreCase = true)
        }
    }

    fun isFull(event: Event): Boolean = event.bookedCount >= event.capacity
}
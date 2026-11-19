package com.example.myapplication.events

object EventRules {

    // Assumed limits, adjust to whatever the team writes in its report.
    const val MAX_CAPACITY = 1000
    const val MAX_POINTS = 100

    // Returns an error message to show the user, or null if the input is valid.
    fun validate(
        title: String,
        venue: String,
        startTime: Long,
        endTime: Long,
        capacity: Int,
        pointsRequested: Int,
        now: Long = System.currentTimeMillis()
    ): String? {
        if (title.isBlank()) return "Title is required"
        if (venue.isBlank()) return "Venue is required"
        if (startTime <= 0L || endTime <= 0L) return "Start and end time are required"
        if (startTime <= now) return "Start time must be in the future"
        if (endTime <= startTime) return "End time must be after the start time"
        if (capacity < 1 || capacity > MAX_CAPACITY) return "Capacity must be between 1 and $MAX_CAPACITY"
        if (pointsRequested < 1 || pointsRequested > MAX_POINTS) {
            return "Points requested must be between 1 and $MAX_POINTS"
        }
        return null
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
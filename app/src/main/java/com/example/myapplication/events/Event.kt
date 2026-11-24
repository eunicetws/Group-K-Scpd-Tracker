package com.example.myapplication.events

enum class EventStatus { PENDING, APPROVED, REJECTED, CANCELLED }

data class EventSession(
    val startTime: Long = 0L,
    val endTime: Long = 0L
)

data class Event(
    val id: String = "",
    val clubId: String = "",
    val title: String = "",

    // Time, Place
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val sessions: List<EventSession> = emptyList(),
    val venue: String = "",

    // Prices and points
    val memberPrice: Double = 0.0,
    val nonMemberPrice: Double = 0.0,
    val pointsRequested: Int = 0,

    // Description
    val description: String = "",

    // Sign ups
    val targetParticipants: Int = 0,
    val capacity: Int = 0,
    val bookedCount: Int = 0,

    // Category and activity type
    val category: String = "",
    val activityType: String = "",

    // Approval
    val status: EventStatus = EventStatus.PENDING,
    val rejectionReason: String? = null
)

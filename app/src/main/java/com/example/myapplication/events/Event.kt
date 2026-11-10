package com.example.myapplication.events

enum class EventStatus { PENDING, APPROVED, REJECTED, CANCELLED }

data class Event(
    val id: String = "",
    val clubId: String = "",
    val title: String = "",
    val description: String = "",
    val venue: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val capacity: Int = 0,
    val bookedCount: Int = 0,
    val pointsRequested: Int = 0,
    val status: EventStatus = EventStatus.PENDING,
    val rejectionReason: String? = null
)

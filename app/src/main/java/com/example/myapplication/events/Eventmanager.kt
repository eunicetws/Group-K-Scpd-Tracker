package com.example.myapplication.events

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object EventManager {

    private const val EVENTS = "events"

    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    // a club creates an event and submits it for approval. Always starts as PENDING.
    fun createEvent(
        clubId: String,
        title: String,
        description: String,
        venue: String,
        startTime: Long,
        endTime: Long,
        capacity: Int,
        pointsRequested: Int,
        result: (Boolean, String?) -> Unit   // (success, new event id or error message)
    ) {
        val error = EventRules.validate(title, venue, startTime, endTime, capacity, pointsRequested)
        if (error != null) {
            result(false, error)
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            result(false, "Please log in first")
            return
        }

        // Only a committee member of this club may create events for it.
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { user ->
                if (user.getString("club") != clubId) {
                    result(false, "You are not a committee member of this club")
                    return@addOnSuccessListener
                }

                val data = hashMapOf<String, Any>(
                    "clubId" to clubId,
                    "title" to title.trim(),
                    "description" to description.trim(),
                    "venue" to venue.trim(),
                    "startTime" to startTime,
                    "endTime" to endTime,
                    "capacity" to capacity,
                    "bookedCount" to 0,
                    "pointsRequested" to pointsRequested,
                    "status" to EventStatus.PENDING.name,
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                db.collection(EVENTS).add(data)
                    .addOnSuccessListener { ref -> result(true, ref.id) }
                    .addOnFailureListener { e -> result(false, e.message) }
            }
            .addOnFailureListener { e -> result(false, e.message) }
    }

    // "My events" for a club, with each event's status and rejection reason.
    fun getClubEvents(clubId: String, result: (List<Event>, String?) -> Unit) {
        db.collection(EVENTS)
            .whereEqualTo("clubId", clubId)
            .get()
            .addOnSuccessListener { snapshot ->
                val events = snapshot.documents.mapNotNull { toEvent(it) }.sortedBy { it.startTime }
                result(events, null)
            }
            .addOnFailureListener { e -> result(emptyList(), e.message) }
    }

    // The marketplace only ever receives APPROVED events.
    fun getApprovedEvents(result: (List<Event>, String?) -> Unit) {
        db.collection(EVENTS)
            .whereEqualTo("status", EventStatus.APPROVED.name)
            .get()
            .addOnSuccessListener { snapshot ->
                val events = snapshot.documents.mapNotNull { toEvent(it) }.sortedBy { it.startTime }
                result(events, null)
            }
            .addOnFailureListener { e -> result(emptyList(), e.message) }
    }

    // Details can change only while the event is still PENDING. The status is re-read
    // inside a transaction so an admin approving it at the same moment cannot be overwritten.
    fun updateEvent(
        eventId: String,
        title: String,
        description: String,
        venue: String,
        startTime: Long,
        endTime: Long,
        capacity: Int,
        pointsRequested: Int,
        result: (Boolean, String?) -> Unit
    ) {
        val error = EventRules.validate(title, venue, startTime, endTime, capacity, pointsRequested)
        if (error != null) {
            result(false, error)
            return
        }

        val ref = db.collection(EVENTS).document(eventId)
        db.runTransaction { tx ->
            val status = readStatus(tx.get(ref))
            if (!EventRules.canEdit(status)) {
                throw IllegalStateException("This event is ${status.name.lowercase()} and can no longer be edited")
            }
            tx.update(
                ref,
                mapOf(
                    "title" to title.trim(),
                    "description" to description.trim(),
                    "venue" to venue.trim(),
                    "startTime" to startTime,
                    "endTime" to endTime,
                    "capacity" to capacity,
                    "pointsRequested" to pointsRequested
                )
            )
            null
        }
            .addOnSuccessListener { result(true, null) }
            .addOnFailureListener { e -> result(false, e.message) }
    }

    // The only change allowed after approval. Notifying booked students comes later.
    fun cancelEvent(eventId: String, result: (Boolean, String?) -> Unit) {
        val ref = db.collection(EVENTS).document(eventId)
        db.runTransaction { tx ->
            val status = readStatus(tx.get(ref))
            if (!EventRules.canCancel(status)) {
                throw IllegalStateException("This event is ${status.name.lowercase()} and cannot be cancelled")
            }
            tx.update(ref, "status", EventStatus.CANCELLED.name)
            null
        }
            .addOnSuccessListener { result(true, null) }
            .addOnFailureListener { e -> result(false, e.message) }
    }

    private fun readStatus(doc: DocumentSnapshot): EventStatus {
        if (!doc.exists()) throw IllegalStateException("Event not found")
        return EventStatus.entries.firstOrNull { it.name == doc.getString("status") }
            ?: throw IllegalStateException("Event has an invalid status")
    }

    private fun toEvent(doc: DocumentSnapshot): Event? =
        doc.toObject(Event::class.java)?.copy(id = doc.id)
}

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
        event: NewEvent,
        posters: List<PosterUpload>,
        files: List<FileUpload>,
        result: (Boolean, String?) -> Unit   // (success, new event id or error message)
    ) {
        val errors = EventRules.validate(event)
        for (message in errors.values) {
            result(false, message)
            return
        }
        if (!EventRules.hasEpf(files)) {
            result(false, "Upload the EPF")
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

                val sessions = mutableListOf<Map<String, Any>>()
                for ((startTime, endTime) in event.sessions) {
                    val sessionData = hashMapOf<String, Any>(
                        "startTime" to startTime,
                        "endTime" to endTime
                    )
                    sessions.add(sessionData)
                }

                val data = hashMapOf<String, Any>(
                    "clubId" to clubId,
                    "title" to event.title.trim(),
                    "description" to event.description.trim(),
                    "venue" to event.venue.trim(),
                    "startTime" to event.startTime,
                    "endTime" to event.endTime,
                    "sessions" to sessions,
                    "capacity" to event.capacity,
                    "bookedCount" to 0,
                    "pointsRequested" to event.pointsRequested,
                    "memberPrice" to event.memberPrice,
                    "nonMemberPrice" to event.nonMemberPrice,
                    "targetParticipants" to event.targetParticipants,
                    "category" to event.category,
                    "activityType" to event.activityType,
                    "posterCount" to posters.size,
                    "status" to EventStatus.PENDING.name,
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                val eventDocument = db.collection(EVENTS).document()
                val batch = db.batch()
                batch.set(eventDocument, data)

                for (index in posters.indices) {
                    val poster = posters[index]
                    val posterData = hashMapOf<String, Any>(
                        "image" to poster.image,
                        "alternativeText" to poster.alternativeText,
                        "order" to index
                    )
                    batch.set(eventDocument.collection("posters").document(index.toString()), posterData)
                }

                for ((kind, name, mimeType, data1) in files) {
                    val fileData = hashMapOf<String, Any>(
                        "kind" to kind.name,
                        "name" to name,
                        "mimeType" to mimeType,
                        "data" to data1
                    )
                    batch.set(eventDocument.collection("files").document(), fileData)
                }

                batch.commit()
                    .addOnSuccessListener { result(true, eventDocument.id) }
                    .addOnFailureListener { e ->
                        print("Error: $e")
                        result(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                print("Error: $e")
                result(false, e.message)
            }
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
            .addOnFailureListener { e ->
                print("Error: $e")
                result(emptyList(), e.message)
            }
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
            .addOnFailureListener { e ->
                print("Error: $e")
                result(emptyList(), e.message)
            }
    }

    // Details can change only while the event is still PENDING. The status is re-read
    // inside a transaction so an admin approving it at the same moment cannot be overwritten.
    fun updateEvent(
        eventId: String,
        event: NewEvent,
        result: (Boolean, String?) -> Unit
    ) {
        val errors = EventRules.validate(event)
        for (message in errors.values) {
            result(false, message)
            return
        }

        val sessions = mutableListOf<Map<String, Any>>()
        for ((startTime, endTime) in event.sessions) {
            val sessionData = hashMapOf<String, Any>(
                "startTime" to startTime,
                "endTime" to endTime
            )
            sessions.add(sessionData)
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
                    "title" to event.title.trim(),
                    "description" to event.description.trim(),
                    "venue" to event.venue.trim(),
                    "startTime" to event.startTime,
                    "endTime" to event.endTime,
                    "sessions" to sessions,
                    "capacity" to event.capacity,
                    "pointsRequested" to event.pointsRequested,
                    "memberPrice" to event.memberPrice,
                    "nonMemberPrice" to event.nonMemberPrice,
                    "targetParticipants" to event.targetParticipants,
                    "category" to event.category,
                    "activityType" to event.activityType
                )
            )
            null
        }
            .addOnSuccessListener { result(true, null) }
            .addOnFailureListener { e ->
                print("Error: $e")
                result(false, e.message)
            }
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
            .addOnFailureListener { e ->
                print("Error: $e")
                result(false, e.message)
            }
    }

    private fun readStatus(doc: DocumentSnapshot): EventStatus {
        if (!doc.exists()) throw IllegalStateException("Event not found")
        return EventStatus.entries.firstOrNull { it.name == doc.getString("status") }
            ?: throw IllegalStateException("Event has an invalid status")
    }

    private fun toEvent(doc: DocumentSnapshot): Event? =
        doc.toObject(Event::class.java)?.copy(id = doc.id)
}

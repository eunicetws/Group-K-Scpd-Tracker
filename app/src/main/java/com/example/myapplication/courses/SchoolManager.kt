package com.example.myapplication.courses

import com.google.firebase.firestore.FirebaseFirestore

data class School(
    val schoolID: String,
    val schoolName: String,
    val courses: List<Course> = emptyList(),
    val active: Boolean = true
)

object SchoolManager {

    private const val COLLECTION: String = "schools"

    fun getSchools(
        activeFilter: Boolean? = null,   // true = active only, false = inactive only, null = all
        result: (List<School>, String?) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection(COLLECTION)
            .get()
            .addOnSuccessListener { snapshot ->
                val schools = mutableListOf<School>()
                for (doc in snapshot.documents) {
                    val schoolName = doc.getString("schoolName")
                    var active = true
                    if (doc.getBoolean("active") == false) {
                        active = false
                    }

                    if (schoolName != null) {
                        if (activeFilter == null || active == activeFilter) {
                            schools.add(School(doc.id, schoolName, active = active))
                        }
                    }
                }
                result(schools, null)
            }
            .addOnFailureListener { e ->
                result(emptyList(), e.message)
            }
    }

    fun saveSchools(
        schools: List<School>,
        result: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        for ((schoolID, schoolName, _, active) in schools) {
            val data = hashMapOf<String, Any>(
                "schoolName" to schoolName,
                "active" to active
            )
            batch.set(db.collection(COLLECTION).document(schoolID), data)
        }

        batch.commit()
            .addOnSuccessListener { result(true, null) }
            .addOnFailureListener { e -> result(false, e.message) }
    }
}

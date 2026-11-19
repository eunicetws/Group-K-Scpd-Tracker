package com.example.myapplication.courses

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Course(
    val courseID: String,
    val courseName: String,
    var schoolID: String,
    val active: Boolean = true
)

object CourseManager {

    private const val COLLECTION: String = "course"
    private const val SCHOOL_COLLECTION: String = "schools"

    fun getCourse(
        schoolID: String? = null,        // school ID - that school only, null - all schools
        activeFilter: Boolean? = null,   // true - active only, false - inactive, null - all
        result: (List<Course>, String?) -> Unit
    ) {
        var query: Query = FirebaseFirestore.getInstance()
            .collection(COLLECTION)

        // filter by school
        if (schoolID != null) {
            query = query.whereEqualTo("schoolID", schoolID)
        }

        query.get()
            .addOnSuccessListener { snapshot ->
                val courses = mutableListOf<Course>()
                for (doc in snapshot.documents) {
                    val courseName = doc.getString("courseName")
                    val schoolID = doc.getString("schoolID")
                    var active = true
                    if (doc.getBoolean("active") == false) {
                        active = false
                    }

                    if (courseName != null && schoolID != null) {
                        if (activeFilter == null || active == activeFilter) {
                            courses.add(Course(doc.id, courseName, schoolID, active))
                        }
                    }
                }
                result(courses, null)
            }
            .addOnFailureListener { e ->
                result(emptyList(), e.message)
            }
    }
    
    fun getCourseSchool(
        course: Course,
        result: (School?, String?) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection(SCHOOL_COLLECTION)
            .document(course.schoolID)
            .get()
            .addOnSuccessListener { doc ->
                val schoolName = doc.getString("schoolName")
                if (!doc.exists() || schoolName == null) {
                    result(null, "School " + course.schoolID + " not found")
                } else {
                    result(School(doc.id, schoolName), null)
                }
            }
            .addOnFailureListener { e ->
                result(null, e.message)
            }
    }

    fun saveCourse(
        schoolID: String,
        courseName: String,
        result: (String?, String?) -> Unit = { _, _ -> }
    ) {
        if (schoolID.isBlank() || courseName.isBlank()) {
            result(null, "Enter a school and course name")
            return
        }

        // load all courses for one school only
        getCourse(schoolID = schoolID, activeFilter = null) { allCourses, error ->
            if (error != null) {
                result(null, error)
            } else {
                // add new course
                val newID = nextCourseID(allCourses, schoolID)
                val data = hashMapOf(
                    "courseName" to courseName.trim(),
                    "schoolID" to schoolID,
                    "active" to true
                )

                FirebaseFirestore.getInstance()
                    .collection(COLLECTION)
                    .document(newID)
                    .set(data)
                    .addOnSuccessListener { result(newID, null) }
                    .addOnFailureListener { e -> result(null, e.message) }
            }
        }
    }

    // new id is SchoolID_int eg. MCOMP_1
    fun nextCourseID(courses: List<Course>, schoolID: String): String {
        val prefix = schoolID + "_"
        var max = 0

        for ((courseID) in courses) {
            if (courseID.startsWith(prefix)) {
                val numT = courseID.removePrefix(prefix)
                val num = numT.toIntOrNull()
                if (num != null && num > max) {
                    max = num
                }
            }
        }

        return prefix + (max + 1)
    }
}

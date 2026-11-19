package com.example.myapplication

import com.example.myapplication.courses.Course
import com.example.myapplication.courses.CourseManager
import org.junit.Assert.assertEquals
import org.junit.Test

class CourseManagerTest {

    // Makes a course
    private fun course(courseID: String, schoolID: String = "MCOMP"): Course {
        return Course(courseID = courseID, courseName = "Course $courseID", schoolID = schoolID)
    }

    // create first course
    @Test
    fun startingCourseID() {
        val existingCourses = emptyList<Course>()

        val newID = CourseManager.nextCourseID(existingCourses, "MCOMP")

        assertEquals("MCOMP_1", newID)
    }

    // if 1 3, no 2, it should show 4
    @Test
    fun nextCourseID() {
        val existingCourses = listOf(course("MCOMP_1"), course("MCOMP_3"))

        val newID = CourseManager.nextCourseID(existingCourses, "MCOMP")

        assertEquals("MCOMP_4", newID)
    }

    // PSY_5 should not affect count for MCOMP
    @Test
    fun ignoreOtherSchoolCourse() {
        val existingCourses = listOf(course("MCOMP_1"), course("MSCI_5", schoolID = "MSCI"))

        val newID = CourseManager.nextCourseID(existingCourses, "MCOMP")

        assertEquals("MCOMP_2", newID)
    }

    // MCOMP_ there is no num, so skip
    @Test
    fun ignoreNonNumberEnding() {
        val existingCourses = listOf(course("MCOMP_abc"), course("MCOMP_2"))

        val newID = CourseManager.nextCourseID(existingCourses, "MCOMP")

        assertEquals("MCOMP_3", newID)
    }

    // MCOMPX and MCOMP is similar but is not the same
    @Test
    fun ignoreSimilarSchool() {
        val existingCourses = listOf(course("MCOMPX_9", schoolID = "MCOMPX"))

        val newID = CourseManager.nextCourseID(existingCourses, "MCOMP")

        assertEquals("MCOMP_1", newID)
    }
}

package com.example.myapplication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.myapplication.ui.components.LOADING_TEST_TAG
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.user.ProfileContent
import com.example.myapplication.ui.user.ProfileEvent
import com.example.myapplication.ui.user.ProfileInfo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun sampleProfile(
        eventsRegistered: List<ProfileEvent> = emptyList(),
        eventsJoined: List<ProfileEvent> = emptyList()
    ) = ProfileInfo(
        name = "Jane Tan",
        studentID = "20512345",
        libraryNumber = "116372189",
        courseName = "Computer Science",
        year = "2",
        scpdPoints = 15,
        totalEvents = 4,
        eventsRegistered = eventsRegistered,
        eventsJoined = eventsJoined
    )

    private fun showProfile(
        profile: ProfileInfo?,
        message: String = "",
        onRegisteredClick: () -> Unit = {},
        onJoinedClick: () -> Unit = {},
        onSignOut: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileContent(
                    profile = profile,
                    message = message,
                    onRegisteredClick = onRegisteredClick,
                    onJoinedClick = onJoinedClick,
                    onSignOut = onSignOut
                )
            }
        }
    }

    @Test
    fun loadingScreen() {
        showProfile(profile = null)
        composeTestRule.onNodeWithTag(LOADING_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("Loading").assertIsDisplayed()
        assertEquals(2, composeTestRule.onAllNodesWithContentDescription("Duck").fetchSemanticsNodes().size)
        composeTestRule.onNodeWithContentDescription("Goose").assertExists()
    }

    @Test
    fun error() {
        showProfile(profile = null, message = "Could not load profile: offline")
        composeTestRule.onNodeWithText("Could not load profile: offline").assertIsDisplayed()
        composeTestRule.onNodeWithTag(LOADING_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun showsStudentDetails() {
        showProfile(profile = sampleProfile())
        composeTestRule.onNodeWithText("Jane Tan").assertIsDisplayed()
        composeTestRule.onNodeWithText("Student ID: 20512345").assertIsDisplayed()
        composeTestRule.onNodeWithText("Library Number: 116372189").assertIsDisplayed()
        composeTestRule.onNodeWithText("Computer Science").assertIsDisplayed()
        composeTestRule.onNodeWithText("Year 2").assertIsDisplayed()

        composeTestRule.onNodeWithText("Total Scpd Points: 15").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Total Events Joined: 4").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun noEventsYet() {
        showProfile(profile = sampleProfile())
        assertEquals(2, composeTestRule.onAllNodesWithText("No events yet").fetchSemanticsNodes().size)
    }

    @Test
    fun sectionTitles_callTheirOwnAction() {
        var registeredClickCount = 0
        var joinedClickCount = 0
        showProfile(
            profile = sampleProfile(),
            onRegisteredClick = { registeredClickCount++ },
            onJoinedClick = { joinedClickCount++ }
        )

        composeTestRule.onNodeWithText("Events you have Registered").performScrollTo().performClick()
        assertEquals(1, registeredClickCount)
        assertEquals(0, joinedClickCount)

        composeTestRule.onNodeWithText("Events You Have Previously Joined").performScrollTo().performClick()
        assertEquals(1, registeredClickCount)
        assertEquals(1, joinedClickCount)
    }
}

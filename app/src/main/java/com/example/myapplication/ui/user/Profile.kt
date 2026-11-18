package com.example.myapplication.ui.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.courses.CourseManager
import com.example.myapplication.login.AuthManager
import com.example.myapplication.ui.components.Header
import com.example.myapplication.ui.theme.LightBlue
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Navy
import com.example.myapplication.ui.theme.Pink80
import com.google.firebase.firestore.FirebaseFirestore

data class ProfileEvent(
    val eventName: String,
    val clubName: String,
    val posterUrl: String = ""
)

data class ProfileInfo(
    val name: String,
    val studentID: String,
    val libraryNumber: String,
    val courseName: String,
    val year: String,
    val scpdPoints: Long,
    val totalEvents: Long,
    val eventsRegistered: List<ProfileEvent>,
    val eventsJoined: List<ProfileEvent>
)

//TODO: add event here

@Composable
fun Profile(
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {}
) {
    var profile by remember { mutableStateOf<ProfileInfo?>(null) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val user = AuthManager.currentUser
        if (user == null) {
            message = "You are not logged in"
        } else {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { doc ->
                    var name = doc.getString("name")
                    if (name == null) {
                        name = "-"
                    }
                    var studentID = "-"
                    if (doc.get("student_id") != null) {
                        studentID = doc.get("student_id").toString()
                    }
                    var libraryNumber = "-"
                    if (doc.get("libNum") != null) {
                        libraryNumber = doc.get("libNum").toString()
                    }
                    var year = "-"
                    if (doc.getLong("year") != null) {
                        year = doc.getLong("year").toString()
                    }

                    var scpdPoints: Long = 0
                    val pointsValue = doc.getLong("scpd_points")
                    if (pointsValue != null) {
                        scpdPoints = pointsValue
                    }
                    var totalEvents: Long = 0
                    val eventsValue = doc.getLong("total_events")
                    if (eventsValue != null) {
                        totalEvents = eventsValue
                    }

                    profile = ProfileInfo(
                        name = name,
                        studentID = studentID,
                        libraryNumber = libraryNumber,
                        courseName = "-",
                        year = year,
                        scpdPoints = scpdPoints,
                        totalEvents = totalEvents,
                        eventsRegistered = emptyList(),
                        eventsJoined = emptyList()
                    )

                    val courseID = doc.getString("course")
                    val schoolID = doc.getString("school")
                    if (courseID != null) {
                        CourseManager.getCourse(schoolID = schoolID, activeFilter = null) { courses, error ->
                            if (error != null) {
                                message = "Could not load course: $error"
                            }
                            for ((courseID1, courseName) in courses) {
                                if (courseID1 == courseID) {
                                    val current = profile
                                    if (current != null) {
                                        profile = current.copy(courseName = courseName)
                                    }
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    message = "Could not load profile: " + e.message
                }
        }
    }

    ProfileContent(
        profile = profile,
        message = message,
        modifier = modifier,
        onSignOut = {
            AuthManager.logOut()
            onSignOut()
        }
    )
}


@Composable
fun ProfileContent(
    profile: ProfileInfo?,
    message: String,
    modifier: Modifier = Modifier,
    onRegisteredClick: () -> Unit = {},
    onJoinedClick: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Header()

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (profile == null) {
                if (message.isNotEmpty()) {
                    InfoText(message, MaterialTheme.colorScheme.error)
                } else {
                    CircularProgressIndicator(color = Navy)
                }
            } else {
                Text(profile.name, style = MaterialTheme.typography.titleLarge, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                InfoText("Student ID: " + profile.studentID)
                InfoText("Library Number: " + profile.libraryNumber)

                SectionDivider()

                TitleText("Course and Year:")
                Spacer(modifier = Modifier.height(4.dp))
                InfoText(profile.courseName)
                InfoText("Year " + profile.year)

                SectionDivider()

                TitleText("Total Scpd Points: " + profile.scpdPoints)
                Spacer(modifier = Modifier.height(4.dp))
                InfoText("Total Events Joined: " + profile.totalEvents)

                SectionDivider()

                EventSection(
                    title = "Events you have Registered",
                    events = profile.eventsRegistered,
                    onArrowClick = onRegisteredClick
                )
                Spacer(modifier = Modifier.height(16.dp))
                EventSection(
                    title = "Events You Have Previously Joined",
                    events = profile.eventsJoined,
                    onArrowClick = onJoinedClick
                )

                SectionDivider()

                Button(
                    onClick = onSignOut,
                    modifier = Modifier.size(width = 125.dp, height = 52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Pink80,
                        contentColor = Color.Black
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    border = BorderStroke(1.5.dp, Navy)
                ) {
                    TitleText("Sign Out")
                }

                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoText(message, MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun TitleText(text: String) {
    Text(text, style = MaterialTheme.typography.headlineLarge, color = Color.Black)
}

@Composable
private fun InfoText(text: String, color: Color = Color.Black) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = color, textAlign = TextAlign.Center)
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(vertical = 16.dp),
        thickness = 3.dp,
        color = LightBlue
    )
}

@Composable
private fun EventSection(
    title: String,
    events: List<ProfileEvent>,
    onArrowClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onArrowClick()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black
            )
            Icon(
                painter = painterResource(R.drawable.icon_arrowdown),
                contentDescription = "See all",
                modifier = Modifier
                    .size(16.dp)
                    .rotate(-90f),
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (events.isEmpty()) {
            InfoText("No events yet", Color.Gray)
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (event in events) {
                    EventCard(event)
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: ProfileEvent) {
    Column(modifier = Modifier.width(130.dp)) {
        Box(
            modifier = Modifier
                .size(width = 130.dp, height = 170.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(LightBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = event.eventName,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            if (event.posterUrl.isNotEmpty()) {
                AsyncImage(
                    model = event.posterUrl,
                    contentDescription = event.eventName + " poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (event.clubName.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(LightBlue)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(event.clubName, style = MaterialTheme.typography.labelSmall, color = Color.Black, maxLines = 2)
            }
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun ProfilePreview() {
    val registered = mutableListOf<ProfileEvent>()
    registered.add(ProfileEvent("Career Conspiracies Forum", "Conspiracy Society"))
    registered.add(ProfileEvent("Pick & Pray", "Student Association"))

    val joined = mutableListOf<ProfileEvent>()
    joined.add(ProfileEvent("A Future with EY", "Youth Entrepreneur Society"))
    joined.add(ProfileEvent("Saranam", "Music Society"))

    MyApplicationTheme {
        ProfileContent(
            profile = ProfileInfo(
                name = "John Doe",
                studentID = "20853217",
                libraryNumber = "116372189",
                courseName = "Computer Science",
                year = "3",
                scpdPoints = 20,
                totalEvents = 10,
                eventsRegistered = registered,
                eventsJoined = joined
            ),
            message = ""
        )
    }
}

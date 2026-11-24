package com.example.myapplication.ui.clubs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.events.Event
import com.example.myapplication.events.EventStatus
import com.example.myapplication.ui.AppTab
import com.example.myapplication.ui.NavigationBar
import com.example.myapplication.ui.components.HeaderBack
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.HeadingStyle
import com.example.myapplication.ui.theme.LabelStyle
import com.example.myapplication.ui.theme.LabelWhiteStyle
import com.example.myapplication.ui.theme.LightBlue
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Navy
import com.example.myapplication.ui.theme.Pink80
import com.example.myapplication.ui.theme.PillButtonStyle
import com.example.myapplication.ui.theme.SmallWhiteStyle
import com.example.myapplication.ui.theme.ValueStyle
import com.example.myapplication.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//Club detail screen shows logo, description, join/leave button, and upcoming/previous events
@Composable
fun ClubDetailPage(
    club: Club?,
    events: List<Event>,
    onBack: () -> Unit
) {
    //Local membership state (TODO: link this to Firestore later)
    var joined by remember { mutableStateOf(false) }

    //Split events by time: not started yet = upcoming, already started = previous
    val now = remember { System.currentTimeMillis() }
    val upcoming = mutableListOf<Event>()
    val previous = mutableListOf<Event>()
    for (event in events) {
        if (event.startTime >= now) {
            upcoming.add(event)
        } else {
            previous.add(event)
        }
    }
    upcoming.sortBy { event -> event.startTime }
    previous.sortByDescending { event -> event.startTime }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        HeaderBack(onBackClick = onBack)

        //Show loading spinner if club data hasn't loaded yet
        if (club == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Navy)
            }
            return@Column
        }

        //Main content column for club info
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ClubLogo(club, size = 170)
            Spacer(Modifier.height(12.dp))
            Text(club.name, style = HeadingStyle, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))

            //Join button (shown if user hasn't joined yet)
            if (!joined) {
                ClubButton(text = "Join", fillColor = LightBlue, onClick = { joined = true })
                Spacer(Modifier.height(12.dp))
            }

            SectionDivider()

            //Club description text
            Text(
                club.description,
                style = ValueStyle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            SectionDivider()

            //Display event sections if any exist
            if (upcoming.isNotEmpty()) {
                EventSection("Upcoming Events", upcoming)
            }
            if (previous.isNotEmpty()) {
                EventSection("Previous Events", previous)
            }

            //Leave button (shown if user is already a member)
            if (joined) {
                Spacer(Modifier.height(16.dp))
                SectionDivider()
                Spacer(Modifier.height(12.dp))
                ClubButton(text = "Leave", fillColor = Pink80, onClick = { joined = false })
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ClubButton(text: String, fillColor: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = fillColor, contentColor = Black),
        border = BorderStroke(1.5.dp, Navy),
        elevation = null,
        modifier = Modifier.width(140.dp)
    ) {
        Text(text, style = PillButtonStyle)
    }
}

//Reusable horizontal divider line
@Composable
private fun SectionDivider() {
    HorizontalDivider(thickness = 2.dp, color = LightBlue)
}

//Section layout containing a title and a horizontal scrolling row of event cards
@Composable
private fun EventSection(title: String, events: List<Event>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: open full events list */ },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = LabelStyle)
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "See all $title",
                tint = Black
            )
        }
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(events, key = { event -> event.id }) { event ->
                EventCard(event)
            }
        }
    }
}

//Card representing a single event (shows title, date, and venue)
@Composable
private fun EventCard(event: Event) {
    //Format the event start time into a readable date string
    val dateText = remember(event.startTime) {
        SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(Date(event.startTime))
    }
    Column(
        modifier = Modifier
            .size(width = 130.dp, height = 150.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Navy)
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(event.title, style = LabelWhiteStyle, maxLines = 3)
        Column {
            Text(dateText, style = SmallWhiteStyle)
            Text(event.venue, style = SmallWhiteStyle, maxLines = 1)
        }
    }
}

//Preview for testing the detail page UI easily with mock data
@Preview(showBackground = true, heightDp = 800)
@Composable
fun ClubDetailPagePreview() {
    val future = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
    val past = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L

    MyApplicationTheme {
        Scaffold(
            bottomBar = { NavigationBar(selected = AppTab.Clubs, onSelect = {}) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Box(Modifier.padding(padding)) {
                ClubDetailPage(
                    club = Club("club1", "Computer Science Society", "We don't code, we build"),
                    events = listOf(
                        Event(
                            id = "1", clubId = "club1", title = "Hackathon", venue = "Lab 3",
                            startTime = future, endTime = future,
                            capacity = 50, bookedCount = 10, pointsRequested = 5, status = EventStatus.APPROVED
                        ),
                        Event(
                            id = "2", clubId = "club1", title = "Chess Night", venue = "Hall A",
                            startTime = past, endTime = past,
                            capacity = 40, bookedCount = 40, pointsRequested = 3, status = EventStatus.APPROVED
                        )
                    ),
                    onBack = {}
                )
            }
        }
    }
}

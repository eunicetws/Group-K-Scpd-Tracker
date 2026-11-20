package com.example.myapplication.ui.clubs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.events.Event
import com.example.myapplication.events.EventStatus
import com.example.myapplication.ui.AppTab
import com.example.myapplication.ui.NavigationBar
import com.example.myapplication.ui.components.Header
import com.example.myapplication.ui.theme.LightBlue
import com.example.myapplication.ui.theme.Navy
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
    //Local membership state (TODO: link this to Firestore later)s
    var joined by remember { mutableStateOf(false) }

    //Split events by time: not started yet = upcoming, already started = previous
    val now = remember { System.currentTimeMillis() }
    val upcoming = events.filter { it.startTime >= now }.sortedBy { it.startTime }
    val previous = events.filter { it.startTime < now }.sortedByDescending { it.startTime }

    Column(Modifier.fillMaxSize().background(Color.White)) {
        Header()

        //Header only has a right-side slot, so the back arrow gets its own separate row
        IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        //Show loading spinner if club data hasn't loaded yet
        if (club == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        //Main content column for club info
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ClubLogo(club, size = 170)
            Spacer(Modifier.height(12.dp))
            Text(club.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            //Join button (shown if user hasn't joined yet)
            if (!joined) {
                Button(
                    onClick = { joined = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LightBlue, contentColor = Navy),
                    modifier = Modifier.width(140.dp)
                ) { Text("Join", fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(12.dp))
            }

            SectionDivider()

            //Club description text
            Text(
                club.description,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )

            SectionDivider()

            //Display event sections if any exist
            if (upcoming.isNotEmpty()) EventSection("Upcoming Events", upcoming)
            if (previous.isNotEmpty()) EventSection("Previous Events", previous)

            //Leave button (shown if user is already a member)
            if (joined) {
                Spacer(Modifier.height(16.dp))
                SectionDivider()
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { joined = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF4C2D0),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.width(140.dp)
                ) { Text("Leave", fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(24.dp))
        }
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
    Column(Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { /* TODO: open full events list */ },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "See all $title")
        }
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(events, key = { it.id }) { event -> EventCard(event) }
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
            .background(Color(0xFF3B2A5C))
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(event.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 3)
        Column {
            Text(dateText, color = Color.White, fontSize = 11.sp)
            Text(event.venue, color = Color.White, fontSize = 11.sp, maxLines = 1)
        }
    }
}

//Preview for testing the detail page UI easily with mock data
@Preview(showBackground = true, heightDp = 800)
@Composable
fun ClubDetailPagePreview() {
    val future = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
    val past = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L

    Scaffold(
        bottomBar = { NavigationBar(selected = AppTab.Clubs, onSelect = {}) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(Modifier.padding(padding)) {
            ClubDetailPage(
                club = Club("club1", "computer science society", "We don't code, we build"),
                events = listOf(
                    Event("1", "club1", "Hackathon", "", "Lab 3", future, future, 50, 10, 5, EventStatus.APPROVED),
                    Event("2", "club1", "Chess Night", "", "Hall A", past, past, 40, 40, 3, EventStatus.APPROVED)
                ),
                onBack = {}
            )
        }
    }
}
package com.example.myapplication.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.events.Event
import com.example.myapplication.events.EventManager
import com.example.myapplication.events.EventRules
import com.example.myapplication.events.EventStatus
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.ErrorStyle

// Takes its data as a parameter, so a preview can feed it fake events.
@Composable
fun EventsPage(events: List<Event>, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    val shown = EventRules.search(events, query)

    Column(modifier.padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search events") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        if (shown.isEmpty()) {
            Text("No events found")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(shown, key = { it.id }) { EventCard(it) }
            }
        }
    }
}

@Composable
private fun EventCard(event: Event) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(event.title, style = MaterialTheme.typography.headlineLarge)
            Text(event.venue)
            Text("${event.pointsRequested} points · ${event.bookedCount}/${event.capacity} booked")
            Text(if (EventRules.isFull(event)) "Full" else event.status.name)
        }
    }
}

private val sampleEvents = listOf(
    Event(
        id = "1", clubId = "club1", title = "Chess Night", venue = "Hall A",
        pointsRequested = 3, capacity = 40, bookedCount = 12, status = EventStatus.APPROVED
    ),
    Event(
        id = "2", clubId = "club1", title = "Hackathon", venue = "Lab 3",
        pointsRequested = 5, capacity = 50, bookedCount = 50, status = EventStatus.APPROVED
    )
)

@Composable
fun EventsPageFirebase(modifier: Modifier = Modifier) {
    val inPreviewPane = LocalInspectionMode.current
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!inPreviewPane) {
            EventManager.getApprovedEvents { loadedEvents, message ->
                events = loadedEvents
                error = message
            }
        }
    }

    Column(modifier) {
        if (error != null) {
            Text(
                "Could not load events: $error",
                style = ErrorStyle,
                modifier = Modifier.padding(16.dp)
            )
        }
        EventsPage(events)
    }
}

@Preview(showSystemUi = true)
@Composable
fun EventsPagePreview() {
    MyApplicationTheme { EventsPageFirebase() }
}

@Preview(showSystemUi = true)
@Composable
fun EventsPageSamplePreview() {
    MyApplicationTheme { EventsPage(sampleEvents) }
}

@Preview(showSystemUi = true)
@Composable
fun EventsPageEmptyPreview() {
    MyApplicationTheme { EventsPage(emptyList()) }
}
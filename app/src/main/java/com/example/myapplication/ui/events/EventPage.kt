package com.example.myapplication.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.events.Event
import com.example.myapplication.events.EventRules
import com.example.myapplication.events.EventStatus
import com.example.myapplication.ui.theme.MyApplicationTheme

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
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            Text(event.venue)
            Text("${event.pointsRequested} points · ${event.bookedCount}/${event.capacity} booked")
            Text(if (EventRules.isFull(event)) "Full" else event.status.name)
        }
    }
}

private val sampleEvents = listOf(
    Event("1", "club1", "Chess Night", "", "Hall A", 0, 0, 40, 12, 3, EventStatus.APPROVED),
    Event("2", "club1", "Hackathon", "", "Lab 3", 0, 0, 50, 50, 5, EventStatus.APPROVED)
)

@Preview(showSystemUi = true)
@Composable
fun EventsPagePreview() {
    MyApplicationTheme { EventsPage(sampleEvents) }
}

@Preview(showSystemUi = true)
@Composable
fun EventsPageEmptyPreview() {
    MyApplicationTheme { EventsPage(emptyList()) }
}
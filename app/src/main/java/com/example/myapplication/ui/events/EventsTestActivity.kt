package com.example.myapplication.ui.events

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myapplication.events.Event
import com.example.myapplication.events.EventStatus
import com.example.myapplication.ui.theme.MyApplicationTheme

class EventsTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                EventsPage(
                    listOf(Event("1", "c", "Chess Night", venue = "Hall A", capacity = 40, status = EventStatus.APPROVED))
                )
            }
        }
    }
}
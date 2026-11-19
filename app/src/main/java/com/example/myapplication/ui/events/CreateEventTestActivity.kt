package com.example.myapplication.ui.events

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.myapplication.login.AuthManager
import com.example.myapplication.ui.login.LoginPage
import com.example.myapplication.ui.theme.MyApplicationTheme

// Temporary host for trying the Create Event screen against Firebase.
class CreateEventTestActivity : ComponentActivity() {

    private val viewModel: CreateEventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                var loggedIn by remember { mutableStateOf(AuthManager.isLoggedIn()) }
                if (!loggedIn) {
                    LoginPage(onLoginSuccess = { loggedIn = true })
                } else {
                    CreateEventPage(
                        state = viewModel.ui,
                        onChange = viewModel::onFieldChange,
                        onSubmit = { viewModel.submit(TEST_CLUB_ID) },
                        modifier = Modifier.safeDrawingPadding()
                    )
                }
            }
        }
    }

    companion object {
        // Replace club-id when possible.
        // For now paste the "club" value from the committee user's document in Firestore.
        private const val TEST_CLUB_ID = "PASTE-CLUB-ID-HERE"
    }
}
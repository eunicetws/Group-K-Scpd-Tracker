package com.example.myapplication.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.clubs.ClubDetailPage
import com.example.myapplication.ui.clubs.ClubsViewModel
import com.example.myapplication.ui.clubs.ViewAllClubsPage
import com.example.myapplication.ui.components.Header

//The logged-in app, bottom nav bar shared by every tab, and the page for the selected tab
@Composable
fun MainApp() {
    var tab by rememberSaveable { mutableStateOf(AppTab.Home) }

    Scaffold(
        bottomBar = { NavigationBar(selected = tab, onSelect = { tab = it }) },
        //Header already handles the status bar, so don't add top padding here
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                AppTab.Home -> PlaceholderTab("Home")         // TODO: home page
                AppTab.Calendar -> PlaceholderTab("Calendar") // TODO: calendar page
                AppTab.Scan -> PlaceholderTab("Scan QR")      // TODO: QR scan page
                AppTab.Clubs -> ClubsTab()
                AppTab.Profile -> PlaceholderTab("Profile")   // TODO: profile page
            }
        }
    }
}
//clubs tab shows the list, or one club's details, after tapping it
@Composable
private fun ClubsTab() {
    val vm: ClubsViewModel = viewModel()
    val clubs by vm.clubs.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val selectedClub by vm.selectedClub.collectAsState()
    val events by vm.events.collectAsState()
    var selectedId by remember { mutableStateOf<String?>(null) }

    if (selectedId == null) {
        ViewAllClubsPage(
            clubs = clubs,
            isLoading = isLoading,
            onClubClick = { club ->
                selectedId = club.id
                vm.loadClub(club.id)
            }
        )
    } else {
        BackHandler { selectedId = null }
        ClubDetailPage(
            club = selectedClub,
            events = events,
            onBack = { selectedId = null }
        )
    }
}

//Temporary screen for tabs that we haven't built yet
@Composable
private fun PlaceholderTab(name: String) {
    Column(Modifier.fillMaxSize()) {
        Header()
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("$name page coming soon")
        }
    }
}
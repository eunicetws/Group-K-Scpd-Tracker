package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.Navy
import com.example.myapplication.ui.theme.White

//The 5 tabs homepage, calendar, scan, clubs and profile
enum class AppTab(val label: String, val icon: Int) {
    Home("Home", R.drawable.icon_home),
    Calendar("Calendar", R.drawable.icon_calander),
    Scan("Scan QR", R.drawable.icon_scan),
    Clubs("Clubs & Society", R.drawable.icon_clubs),
    Profile("Profile", R.drawable.icon_profile)
}

//the bar at the bottom, mainApp.kt decides which page to show
@Composable
fun NavigationBar(
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Navy)
            .navigationBarsPadding()   // keeps it above the phone's naviagtion bar
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (tab in AppTab.entries) {
            TabButton(tab = tab, isSelected = tab == selected, onClick = { onSelect(tab) })
        }
    }
}

@Composable
private fun TabButton(tab: AppTab, isSelected: Boolean, onClick: () -> Unit) {
    var circle = Modifier
        .size(44.dp)
        .clip(CircleShape)
    var iconColor = White
    if (isSelected) {
        circle = circle.background(White)
        iconColor = Navy
    }

    var iconSize = 32.dp
    if (tab == AppTab.Scan) {
        iconSize = 26.dp
    }

    Box(
        modifier = circle.clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(tab.icon),
            contentDescription = tab.label,
            modifier = Modifier.size(iconSize),
            tint = iconColor
        )
    }
}

@Preview
@Composable
fun NavigationBarPreview() {
    NavigationBar(selected = AppTab.Clubs, onSelect = {})
}

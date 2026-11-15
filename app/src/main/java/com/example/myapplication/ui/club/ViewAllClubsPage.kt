package com.example.myapplication.ui.clubs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.Header
import com.example.myapplication.ui.theme.LightBlue
import androidx.compose.material.icons.filled.FilterList

//View all clubs page (Stateless: clubs are passed from clubs viewmodel)
@Composable
fun ViewAllClubsPage(
    clubs: List<Club>,
    isLoading: Boolean = false,
    onClubClick: (Club) -> Unit
) {
    //Search query state and filtered list
    var query by remember { mutableStateOf("") }
    val filtered = clubs.filter { it.name.contains(query, ignoreCase = true) }
    //Screen layout
    Column(Modifier.fillMaxSize().background(Color.White)) {
        //Top app header
        Header(
            actions = {
                Text(
                    "Clubs & Society",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        )
        //Search bar and filter button row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(50),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = LightBlue,
                    unfocusedContainerColor = LightBlue,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
            )
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = { }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
            }
        }
        //Page section title
        Text(
            "Clubs & Society",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        //Loading spinner, empty message, or the club grid
        when {
            isLoading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            filtered.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No clubs found")
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filtered, key = { it.id }) { club ->
                    ClubGridItem(club, onClick = { onClubClick(club) })
                }
            }
        }
    }
}
//Each club with logo and name, when clicked, it opens clubs details page
@Composable
private fun ClubGridItem(club: Club, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        ClubLogo(club, size = 90)
        Spacer(Modifier.height(8.dp))
        Text(
            club.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
//Preview for testing the UI easily
@Preview(showBackground = true, heightDp = 800)
@Composable
fun ViewAllClubsPreview() {
    ViewAllClubsPage(
        clubs = listOf(
            Club("club1", "computer science society", "We don't code, we build")
        ),
        onClubClick = {}
    )
}
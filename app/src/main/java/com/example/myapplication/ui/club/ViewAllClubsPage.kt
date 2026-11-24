package com.example.myapplication.ui.clubs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.Header
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.SectionTitleStyle
import com.example.myapplication.ui.theme.LightBlue
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Navy
import com.example.myapplication.ui.theme.HeadingStyle
import com.example.myapplication.ui.theme.PlaceholderStyle
import com.example.myapplication.ui.theme.ValueStyle
import com.example.myapplication.ui.theme.White

//View all clubs page (Stateless: clubs are passed from clubs viewmodel)
@Composable
fun ViewAllClubsPage(
    clubs: List<Club>,
    isLoading: Boolean = false,
    onClubClick: (Club) -> Unit
) {
    //Search query state and filtered list
    var query by remember { mutableStateOf("") }
    val filtered = mutableListOf<Club>()
    for (club in clubs) {
        if (club.name.contains(query.trim(), ignoreCase = true)) {
            filtered.add(club)
        }
    }

    //Screen layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        //Top app header
        Header(title = "Clubs & Society")

        //Search bar and filter button row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                query = query,
                onQueryChange = { newQuery -> query = newQuery },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Black)
            }
        }

        //Page section title
        Text(
            "Clubs & Society",
            style = HeadingStyle,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp)
        )

        //Loading spinner, empty message, or the club grid
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Navy)
            }
        } else if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No clubs found", style = PlaceholderStyle)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filtered, key = { club -> club.id }) { club ->
                    ClubGridItem(club, onClick = { onClubClick(club) })
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(LightBlue)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = "Search",
            tint = Black,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = ValueStyle,
            cursorBrush = SolidColor(Navy),
            modifier = Modifier.weight(1f),
            decorationBox = { innerField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text("Search clubs", style = PlaceholderStyle)
                    }
                    innerField()
                }
            }
        )
    }
}

//Each club with logo and name, when clicked, it opens clubs details page
@Composable
private fun ClubGridItem(club: Club, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        ClubLogo(club, size = 100)
        Spacer(Modifier.height(10.dp))
        Text(
            club.name,
            style = SectionTitleStyle,
            textAlign = TextAlign.Center
        )
    }
}

//Preview for testing the UI easily
@Preview(showBackground = true, heightDp = 800)
@Composable
fun ViewAllClubsPreview() {
    MyApplicationTheme {
        ViewAllClubsPage(
            clubs = listOf(
                Club("club1", "Music Society", "Music for everyone"),
                Club("club2", "Youth Entrepreneur Society", "Build your own business"),
                Club("club3", "Literature and Drama Society", "Stories on and off stage"),
                Club("club4", "Conspiracy Society", "Question everything"),
                Club("club5", "Computer Science Society", "We don't code, we build"),
                Club("club6", "Writers Society", "Write it down")
            ),
            onClubClick = {}
        )
    }
}

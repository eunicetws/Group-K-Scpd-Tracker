package com.example.myapplication.ui.clubs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.LabelWhiteStyle
import com.example.myapplication.ui.theme.Navy

//To show club profile pictures (or fallback to initials if no URL exists)
@Composable
fun ClubLogo(club: Club, size: Int, modifier: Modifier = Modifier) {
    //Check if the club has a valid logo image URL
    if (club.logoUrl.isNotBlank()) {
        //Load and display the logo image from the URL using Coil
        AsyncImage(
            model = club.logoUrl,
            contentDescription = club.name,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size.dp).clip(CircleShape)
        )
    } else {
        //Fallback - show a coloured circle with the club's first initial if theres no logo
        Box(
            modifier = modifier.size(size.dp).clip(CircleShape).background(Navy),
            contentAlignment = Alignment.Center
        ) {
            var initial = "?"
            if (club.name.isNotEmpty()) {
                initial = club.name.substring(0, 1).uppercase()
            }
            Text(text = initial, style = LabelWhiteStyle)
        }
    }
}
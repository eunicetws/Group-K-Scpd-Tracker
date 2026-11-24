package com.example.myapplication.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val Navy = Color(0xFF14294A)
val LightBlue = Color(0xFFB4C4D9)

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val Grey = Color(0xFF888888)
val LightGrey = Color(0xFFCCCCCC)
val Red = Color(0xFFB3261E)

@Composable
fun boxColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Black,
        unfocusedBorderColor = Black,
        focusedTextColor = Black,
        unfocusedTextColor = Black,
        focusedLabelColor = Black,
        unfocusedLabelColor = Black,
        focusedPlaceholderColor = Grey,
        unfocusedPlaceholderColor = Grey,
        cursorColor = Black
    )
}
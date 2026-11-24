package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

val Roboto = FontFamily.SansSerif
val Righteous = FontFamily(Font(R.font.righteous))

// Set of Material typography styles to start with
val Typography = Typography(
    bodyMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // section titles, e.g. "Course and Year:", "Total Scpd Points", Sign Out
    headlineLarge = TextStyle(
        fontFamily = Righteous,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,   // bigger than bodyMedium (16) underneath
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp
    ),
    // Figma: Righteous Regular 26, scaled down for phone (profile name)
    titleLarge = TextStyle(
        fontFamily = Righteous,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        letterSpacing = 0.sp
    ),
    // Roboto Bold 16, labels on the Create Event page, e.g. "Venue:" (same size as bodyMedium)
    titleSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Figma: Roboto Regular 20, scaled down for phone, letter spacing 0
    headlineSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.sp
    ),
    // small text, e.g. club name under an event poster
    labelSmall = TextStyle(
        fontFamily = Righteous,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp
    ),

    displayLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

val LabelStyle = Typography.titleSmall.copy(color = Black)              // "Venue:", "Date, Time:"
val LabelWhiteStyle = Typography.titleSmall.copy(color = White)         // club initials on navy
val ValueStyle = Typography.bodyMedium.copy(color = Black)              // typed values and normal text
val ValueWhiteStyle = Typography.bodyMedium.copy(color = White)         // text on dark backgrounds
val PlaceholderStyle = Typography.bodyMedium.copy(color = Grey)         // hints like "Event title", "No dates yet"
val ErrorStyle = Typography.bodySmall.copy(color = Red)                 // error messages
val SectionTitleStyle = Typography.bodySmall.copy(color = Black)        // "Upload EPF", "Additional Attachments"
val HeadingStyle = Typography.headlineLarge.copy(color = Black)         // club name
val ButtonStyle = Typography.bodyMedium                                 // outlined buttons, colour comes from the button
val PillButtonStyle = Typography.headlineLarge                          // Submit, colour comes from the button
val DialogTitleStyle = TextStyle(                                       // popup titles, e.g. "Add Date"
    fontFamily = Roboto,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp,
    color = Black
)
val DialogHeadlineStyle = Typography.headlineMedium.copy(color = Black)  // picked date at the top of the calendar
val SmallWhiteStyle = Typography.labelMedium.copy(color = White)

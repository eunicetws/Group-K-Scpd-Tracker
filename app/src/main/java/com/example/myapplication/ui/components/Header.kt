package com.example.myapplication.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.example.myapplication.R
import com.example.myapplication.ui.theme.LabelWhiteStyle
import com.example.myapplication.ui.theme.Navy
import com.example.myapplication.ui.theme.White

// Shared header
@Composable
fun Header(modifier: Modifier = Modifier, title: String? = null) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Navy)
            .statusBarsPadding()
            .fillMaxHeight(0.08f)
            .padding(horizontal = 20.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.logo_unm_02),
            contentDescription = "University of Nottingham Malaysia",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight(0.6f)
        )

        if (title != null) {
            Text(
                title,
                style = LabelWhiteStyle,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Image(
            painter = painterResource(R.drawable.logo_scpd_white),
            contentDescription = "sCPD logo",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(0.5f)
        )
    }
}

// Login header
@Composable
fun HeaderLogin(
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Navy)
            .statusBarsPadding()
            .fillMaxHeight(0.08f)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(R.drawable.logo_unm_02),
            contentDescription = "University of Nottingham Malaysia",
            modifier = Modifier.fillMaxHeight(0.6f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}

@Composable
fun HeaderBack(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Navy)
            .statusBarsPadding()
            .fillMaxHeight(0.08f)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clickable {
                    onBackClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.icon_arrowdown),
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .rotate(90f),
                tint = White
            )
        }

        Image(
            painter = painterResource(R.drawable.logo_scpd_white),
            contentDescription = "sCPD logo",
            modifier = Modifier.fillMaxHeight(0.5f)
        )
    }
}

// Discover header
@Composable
fun HeaderDiscover(
    modifier: Modifier = Modifier,
    onBellClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Navy)
            .statusBarsPadding()
            .fillMaxHeight(0.08f)
            .padding(horizontal = 20.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.logo_unm_02),
            contentDescription = "University of Nottingham Malaysia",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight(0.6f)
        )

        Image(
            painter = painterResource(R.drawable.logo_scpd_white),
            contentDescription = "sCPD logo",
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight(0.5f)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(30.dp)
                .clickable {
                    onBellClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.icon_bell),
                contentDescription = "Notifications",
                modifier = Modifier.size(30.dp),
                tint = White
            )
        }
    }
}

@Preview(name = "Header", showBackground = true, heightDp = 800)
@Composable
fun HeaderPreview() {
    Column(Modifier.fillMaxSize()) {
        Header()
    }
}

@Preview(name = "Header - Title", showBackground = true, heightDp = 800)
@Composable
fun HeaderTitlePreview() {
    Column(Modifier.fillMaxSize()) {
        Header(title = "Clubs & Society")
    }
}

@Preview(name = "Header - Login", showBackground = true, heightDp = 800)
@Composable
fun HeaderLoginPreview() {
    Column(Modifier.fillMaxSize()) {
        HeaderLogin()
    }
}

@Preview(name = "Header - Back", showBackground = true, heightDp = 800)
@Composable
fun HeaderBackPreview() {
    Column(Modifier.fillMaxSize()) {
        HeaderBack()
    }
}

@Preview(name = "Header - Discover", showBackground = true, heightDp = 800)
@Composable
fun HeaderDiscoverPreview() {
    Column(Modifier.fillMaxSize()) {
        HeaderDiscover()
    }
}

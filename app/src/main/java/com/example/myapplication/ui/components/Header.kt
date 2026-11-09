package com.example.myapplication.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.example.myapplication.R
import com.example.myapplication.ui.theme.Navy

@Composable
fun Header(
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

@Preview(showBackground = true, heightDp = 800)
@Composable
fun HeaderPreview() {
    Column(Modifier.fillMaxSize()) {
        Header()
    }
}

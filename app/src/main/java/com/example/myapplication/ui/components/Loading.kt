package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme

object LoadingAnimation {
    const val ANIMAL_COUNT = 3

    const val CHANGE = ANIMAL_COUNT * 2 // loop 0 1 2

    const val MILLISECONDS = 600

    // animal counts up from 0 to 3, then back down to 1, then starts again
    fun animalCount(i: Int): Int {
        val j = Math.floorMod(i, CHANGE)
        if (j <= ANIMAL_COUNT) {
            return j
        }
        return CHANGE - j
    }
}

const val LOADING_TEST_TAG = "loading"

@Composable
fun Loading(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "loading")
    val progress by transition.animateFloat(

        // animation
        initialValue = 0f,
        targetValue = LoadingAnimation.CHANGE.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = LoadingAnimation.CHANGE * LoadingAnimation.MILLISECONDS,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading ${LoadingAnimation.ANIMAL_COUNT}"
    )

    // animal sequence
    val i = progress.toInt() + LoadingAnimation.ANIMAL_COUNT
    val animalsShown = LoadingAnimation.animalCount(i)

    // body
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag(LOADING_TEST_TAG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Animal(R.drawable.icon_duck, "Duck", 66.dp, animalsShown >= 1)
            Animal(R.drawable.icon_duck, "Duck", 66.dp, animalsShown >= 2)
            Animal(R.drawable.icon_goose, "Goose", 96.dp, animalsShown >= 3)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // load the dots sync with duck
        Row {
            LoadingText("Loading", true)
            LoadingText(" .", animalsShown >= 1)
            LoadingText(" .", animalsShown >= 2)
            LoadingText(" .", animalsShown >= 3)
        }
    }
}

@Composable
private fun LoadingText(text: String, shown: Boolean) {
    var alpha = 0f
    if (shown) {
        alpha = 1f
    }

    // The .
    Text(
        text = text,
        modifier = Modifier.alpha(alpha),
        style = MaterialTheme.typography.titleLarge.copy(fontSize = 36.sp),
        color = Color.Black
    )
}

@Composable
private fun Animal(image: Int, description: String, size: Dp, shown: Boolean) {
    var alpha = 0f
    if (shown) {
        alpha = 1f
    }

    Image(
        painter = painterResource(image),
        contentDescription = description,
        modifier = Modifier
            .size(size)
            .alpha(alpha)
    )
}

@Preview(name = "Loading", showBackground = true, heightDp = 800)
@Composable
fun LoadingPreview() {
    MyApplicationTheme {
        Column(Modifier.fillMaxSize()) {
            Header()
            Loading(modifier = Modifier.weight(1f))
        }
    }
}

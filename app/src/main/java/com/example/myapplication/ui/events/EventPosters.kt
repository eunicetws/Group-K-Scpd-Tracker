package com.example.myapplication.ui.events

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.content.res.ResourcesCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import com.example.myapplication.R
import com.example.myapplication.events.EventMedia
import com.example.myapplication.events.PosterUpload
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.Grey
import com.example.myapplication.ui.theme.LightBlue
import com.example.myapplication.ui.theme.LightGrey
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Navy
import com.example.myapplication.ui.theme.PlaceholderStyle
import com.example.myapplication.ui.theme.ValueWhiteStyle
import com.example.myapplication.ui.theme.White
import androidx.core.graphics.createBitmap

@Composable
fun PosterImage(
    poster: PosterUpload,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (EventMedia.isLink(poster.image)) {
        SubcomposeAsyncImage(
            model = poster.image,
            contentDescription = poster.alternativeText,
            contentScale = contentScale,
            modifier = modifier,
            loading = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            },
            error = { PosterUnavailable(poster.alternativeText) }
        )
    } else {
        val bitmap = remember(poster.image) { EventMedia.decodeBase64Image(poster.image) }
        if (bitmap == null) {
            PosterUnavailable(poster.alternativeText, modifier)
        } else {
            Image(
                bitmap = bitmap,
                contentDescription = poster.alternativeText,
                contentScale = contentScale,
                modifier = modifier
            )
        }
    }
}

@Composable
fun PosterUnavailable(alternativeText: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightGrey)
            .semantics { contentDescription = alternativeText },
        contentAlignment = Alignment.Center
    ) {
        Text("Poster unavailable", style = PlaceholderStyle, textAlign = TextAlign.Center)
    }
}

@Composable
fun PosterCarousel(
    posters: List<PosterUpload>,
    index: Int,
    onIndexChange: (Int) -> Unit,
    onPosterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CarouselArrow(
            description = "Previous poster",
            rotation = 90f,
            enabled = index > 0,
            onClick = { onIndexChange(index - 1) }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(0.707f)
                .shadow(4.dp)
                .background(LightGrey),
            contentAlignment = Alignment.Center
        ) {
            if (posters.isEmpty()) {
                Text("No poster yet", style = PlaceholderStyle)
            } else {
                PosterImage(
                    poster = posters[index],
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onPosterClick() }
                )
            }
        }

        CarouselArrow(
            description = "Next poster",
            rotation = -90f,
            enabled = index < posters.size - 1,
            onClick = { onIndexChange(index + 1) }
        )
    }
}

@Composable
private fun CarouselArrow(description: String, rotation: Float, enabled: Boolean, onClick: () -> Unit) {
    var tint = Black
    if (!enabled) {
        tint = LightGrey
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_arrowdown),
            contentDescription = description,
            modifier = Modifier
                .size(22.dp)
                .rotate(rotation),
            tint = tint
        )
    }
}

@Composable
fun PosterFullScreen(poster: PosterUpload, onClose: () -> Unit) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Black)
        ) {
            PosterImage(
                poster = poster,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            var newScale = scale * zoom
                            if (newScale < 1f) {
                                newScale = 1f
                            }
                            if (newScale > 4f) {
                                newScale = 4f
                            }
                            scale = newScale

                            if (scale == 1f) {
                                offset = Offset.Zero
                            } else {
                                offset = offset + pan
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
            )

            TextButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Black.copy(alpha = 0.5f))
            ) {
                Text("Close", style = ValueWhiteStyle)
            }
        }
    }
}

@SuppressLint("UseKtx")
fun samplePoster(
    title: String,
    backgroundColor: Int,
    textColor: Int = White.toArgb(),
    font: android.graphics.Typeface? = null
): PosterUpload {
    val width = 707
    val height = 1000
    val bitmap = createBitmap(width, height)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(backgroundColor)

    val paint = android.graphics.Paint()
    paint.color = textColor
    paint.textSize = 90f
    if (font == null) {
        paint.isFakeBoldText = true
    } else {
        paint.typeface = font
    }
    paint.textAlign = android.graphics.Paint.Align.CENTER
    canvas.drawText(title, width / 2f, height / 2f, paint)

    val output = java.io.ByteArrayOutputStream()
    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, output)
    val image = android.util.Base64.encodeToString(output.toByteArray(), android.util.Base64.NO_WRAP)
    return PosterUpload(image = image, alternativeText = "$title poster")
}

@Preview(name = "Poster carousel", showBackground = true)
@Composable
fun PosterCarouselPreview() {
    val context = LocalContext.current
    val righteous = ResourcesCompat.getFont(context, R.font.righteous)
    val posters = listOf(
        samplePoster("MUSIC", LightBlue.toArgb(), Navy.toArgb(), righteous),
        samplePoster("FESTIVAL", Navy.toArgb(), White.toArgb(), righteous)
    )
    var index by remember { mutableIntStateOf(0) }

    MyApplicationTheme {
        PosterCarousel(
            posters = posters,
            index = index,
            onIndexChange = { newIndex -> index = newIndex },
            onPosterClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Poster carousel - empty", showBackground = true)
@Composable
fun PosterCarouselEmptyPreview() {
    MyApplicationTheme {
        PosterCarousel(
            posters = emptyList(),
            index = 0,
            onIndexChange = {},
            onPosterClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Poster unavailable", showBackground = true, widthDp = 200, heightDp = 283)
@Composable
fun PosterUnavailablePreview() {
    MyApplicationTheme {
        PosterUnavailable(alternativeText = "Music Festival poster")
    }
}

@Preview(name = "Poster image - broken", showBackground = true, widthDp = 200, heightDp = 283)
@Composable
fun PosterImageBrokenPreview() {
    MyApplicationTheme {
        PosterImage(
            poster = PosterUpload(image = "not a real image", alternativeText = "Broken poster"),
            modifier = Modifier.fillMaxSize()
        )
    }
}

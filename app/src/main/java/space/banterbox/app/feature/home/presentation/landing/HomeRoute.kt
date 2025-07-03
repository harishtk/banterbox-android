package space.banterbox.app.feature.home.presentation.landing

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import space.banterbox.app.ui.DevicePreviews
import space.banterbox.app.ui.theme.BanterboxTheme
import kotlinx.coroutines.delay

@Composable
internal fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {

    // TODO: extract states and actions from view model(s)

    HomeScreen(
        modifier = modifier,
    )
}

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
) {

    // This code should be called when UI is ready for use and relates to Time To Full Display.
    ReportDrawnWhen { true /* Add custom conditions here. eg. !isSyncing */ }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Home", style = MaterialTheme.typography.displayMedium)
        }
    }
}

@Composable
private fun FlippingTitle(
    title: String,
    rotated: Boolean,
) {
    val animateColor by animateColorAsState(
        targetValue = if (rotated) Color.Red else Color.Blue,
        animationSpec = tween(500),
        label = "Text color animation",
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row {
            val mergedStyle = MaterialTheme.typography.displayMedium
            for (index in 0..<title.length) {
                val c = title[index]

                val animationProgress by animateFloatAsState(
                    targetValue = if (rotated) 1f else 0f,
                    animationSpec = tween(500),
                    label = "Animation Progress"
                )

                val rotation by animateFloatAsState(
                    targetValue = if (rotated) 360f else 0f,
                    animationSpec = tween(500, delayMillis = index * 100, easing = EaseInOut),
                    label = "Rotation state",
                )

                if (index % 2 == 0) {
                    AnimatedText(
                        text = c.toString(),
                        style = mergedStyle,
                        color = animateColor,
                        rotationX = rotation,
                    )
                } else {
                    AnimatedText(
                        text = c.toString(),
                        style = mergedStyle,
                        color = animateColor,
                        rotationY = rotation,
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedText(
    text: String,
    style: TextStyle,
    color: Color,
    rotationX: Float = 1f,
    rotationY: Float = 1f,
    translationX: Float = 1f,
    translationY: Float = 1f,
    cameraDistance: Float = 8f,
) {
    Text(
        text = text,
        style = style,
        color = color,
        modifier = Modifier
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationY = rotationY
                this.cameraDistance = cameraDistance
                this.translationX = translationX
                this.translationY = translationY
            }
    )
}

@Preview
@Composable
fun HomeDefaultPreview() {
    BoxWithConstraints(
        Modifier.background(Color.White)
    ) {
        BanterboxTheme {
            HomeScreen()
        }
    }
}
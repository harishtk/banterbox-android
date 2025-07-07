package space.banterbox.app.core.designsystem.component.text

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

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
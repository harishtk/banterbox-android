package space.banterbox.app.feature.home.presentation.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import space.banterbox.app.ui.DevicePreviews
import space.banterbox.app.ui.theme.BanterboxTheme


@Composable
internal fun InsightsRoute(
    modifier: Modifier = Modifier,
) {

    InsightsScreen(
        modifier = modifier,
    )
}

@Composable
internal fun InsightsScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Insights", style = MaterialTheme.typography.displayMedium)
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun InsightsDefaultPreview() {
    BanterboxTheme {
        InsightsScreen()
    }
}
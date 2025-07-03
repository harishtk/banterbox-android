package space.banterbox.app.feature.home.presentation.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import space.banterbox.app.ui.theme.BanterboxTheme

@Composable
internal fun InventoryRoute(
    modifier: Modifier = Modifier,
) {

    InventoryScreen(
        modifier = modifier,
    )
}

@Composable
internal fun InventoryScreen(
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
            Text(text = "Inventory", style = MaterialTheme.typography.displayMedium)
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun InventoryDefaultPreview() {
    BanterboxTheme {
        InventoryScreen()
    }
}
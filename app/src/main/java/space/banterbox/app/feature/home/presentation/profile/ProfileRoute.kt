package space.banterbox.app.feature.home.presentation.profile

import androidx.compose.foundation.background
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
internal fun ProfileRoute(
    modifier: Modifier = Modifier,
) {

    ProfileScreen(
        modifier = modifier,
    )
}

@Composable
private fun ProfileScreen(
    modifier: Modifier,
) {

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
            Text(text = "Profile", style = MaterialTheme.typography.displayMedium)
        }
    }

}

@Preview
@Composable
private fun ProfileScreenPreview() {
    BanterboxTheme {
        ProfileScreen(modifier = Modifier)
    }
}
package space.banterbox.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.banterbox.app.core.designsystem.BanterboxSellerIcons
import space.banterbox.app.core.designsystem.BanterboxTopAppBarState
import space.banterbox.app.ui.theme.BanterboxTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BanterboxTopAppBar(
    modifier: Modifier = Modifier,
    state: BanterboxTopAppBarState,
) {
    Column(
        modifier = modifier
    ) {
        TopAppBar(
            title = {
                Row {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    )
                }
            },
            navigationIcon = {
                if (state.showNavigationIcon) {
                    IconButton(onClick = state.onNavigationIconClick ) {
                        Icon(
                            painter = painterResource(id = BanterboxSellerIcons.Id_ArrowFilled),
                            contentDescription = "Go Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(180f)
                        )
                    }
                }
            },
        )
    }
}

@Composable
@Preview(device = "id:pixel_3a", showBackground = true)
private fun BestDealsTopAppBarPreview() {
    val appBarState = BanterboxTopAppBarState(
        title = "Sample Title",
        showNavigationIcon = true,
        onNavigationIconClick = {}
    )

    BoxWithConstraints(
        Modifier.background(Color.White)
    ) {
        BanterboxTheme(darkTheme = false) {
            BanterboxTopAppBar(state = appBarState)
        }
    }
}
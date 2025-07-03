package space.banterbox.app.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import space.banterbox.app.core.designsystem.ShopsSellerIcons
import space.banterbox.app.ui.theme.BanterboxTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopsTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        windowInsets = windowInsets,
        modifier = modifier.testTag("shopsTpAppBar"),
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview("Top App Bar")
@Composable
private fun ShopsTopAppBarPreview() {
    BanterboxTheme {
        ShopsTopAppBar(
            title = @Composable {
                Text(
                    text = "Sarathas",
                    style = MaterialTheme.typography.titleMedium
                        .copy(fontWeight = FontWeight.W700)
                )
            },
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(id = ShopsSellerIcons.Id_Breadcrumbs),
                        contentDescription = "Open Drawer",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = ShopsSellerIcons.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        )
    }
}
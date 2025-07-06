package space.banterbox.app.core.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import space.banterbox.app.core.designsystem.BanterboxSellerIcons
import space.banterbox.app.core.designsystem.component.navigation.DefaultNavigationBar
import space.banterbox.app.core.designsystem.component.navigation.DefaultNavigationBarItem
import space.banterbox.app.core.designsystem.component.navigation.BanterboxNavigationBarItemDefaults
import space.banterbox.app.ui.theme.NavigationBarBackground
import space.banterbox.app.ui.theme.BanterboxTheme

@Composable
fun RowScope.BanterboxNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    selectedIcon: @Composable () -> Unit,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
    modifier: Modifier = Modifier,
) {
    DefaultNavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = BanterboxNavigationBarItemDefaults.colors()
    )
}

/**
 * Banterbox navigation bar with content slot. Wraps Material 3 [NavigationBar].
 *
 * @param modifier Modifier to be applied to the navigation bar.
 * @param content Destinations inside the navigation bar. This should contain multiple
 * [NavigationBarItem]s.
 */
@Composable
fun BanterboxNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    DefaultNavigationBar(
        modifier = modifier,
        content = content,
    )
}

/**
 * Banterbox navigation rail item with icon and label content slots. Wraps Material 3
 * [NavigationRailItem].
 *
 * @param selected Whether this item is selected.
 * @param onClick The callback to the invoked when this item is selected.
 * @param icon The item icon content.
 * @param selectedIcon the item icon content when selected.
 * @param enabled controls the enabled state of this item. When `false`, this item will not be
 *  * clickable and will appear disabled to accessibility services.
 * @param label The item text label content.
 * @param alwaysShowLabel Whether to always show the label for this item. If false, the label will
 *  * only be shown when this item is selected.
 */
@Composable
fun BanterboxNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    selectedIcon: @Composable () -> Unit,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
    modifier: Modifier = Modifier,
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = BanterboxNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = BanterboxNavigationDefaults.navigationContentColor(),
            selectedTextColor = BanterboxNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = BanterboxNavigationDefaults.navigationContentColor(),
            indicatorColor = BanterboxNavigationDefaults.navigationIndicatorColor(),
        )
    )
}

/**
 * Shop navigation rail with header and content slots. Wraps Material 3 [NavigationRail].
 *
 * @param modifier Modifier to be applied to the navigation rail.
 * @param header Optional header that may hold a floating action button or a logo.
 * @param content Destinations inside the navigation rail. This should contain multiple
 * [NavigationRailItem]s.
 */
@Composable
fun BanterboxNavigationRail(
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    NavigationRail(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = BanterboxNavigationDefaults.navigationContentColor(),
        header = header,
        content = content,
    )
}

/**
 * Banterbox navigation default values.
 */
object BanterboxNavigationDefaults {

    val NavigationBarHeight = 66.dp

    val NavigationBarItemHorizontalPadding = 8.dp

    val Elevation: Dp = Dp.Unspecified

    /** Default color for a navigation bar. */
    val containerColor: Color @Composable get() = NavigationBarBackground

    /**
     * Default window insets to be used and consumed by navigation bar
     */
    val windowInsets: WindowInsets
        @Composable
        get() = NavigationBarDefaults.windowInsets

    @Composable
    fun navigationContentColor() = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun navigationSelectedItemColor() = MaterialTheme.colorScheme.onPrimaryContainer

    @Composable
    fun navigationIndicatorColor() = MaterialTheme.colorScheme.primaryContainer
}

@Preview(group = "navigation bar")
@Composable
fun BanterboxNavigationPreview() {
    val items = listOf("Home", "Insights", null, "Inventory", "Admin")
    val icons = listOf(
        BanterboxSellerIcons.Id_Home_Outline,
        BanterboxSellerIcons.Id_Insights_Outline,
        BanterboxSellerIcons.Id_New,
        BanterboxSellerIcons.Id_Inventory_Outline,
        BanterboxSellerIcons.Id_Admin_Outline,
    )
    val selectedIcons = listOf(
        BanterboxSellerIcons.Id_Home_Filled,
        BanterboxSellerIcons.Id_Insights_Filled,
        BanterboxSellerIcons.Id_New,
        BanterboxSellerIcons.Id_Inventory_Filled,
        BanterboxSellerIcons.Id_Admin_Filled,
    )

    var selectedIndex by remember { mutableIntStateOf(0) }

    BanterboxTheme {
        BanterboxNavigationBar {
            items.forEachIndexed { index, item ->
                BanterboxNavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = icons[index]),
                            contentDescription = item,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            painter = painterResource(id = selectedIcons[index]),
                            contentDescription = item,
                        )
                    },
                    label = if (item != null) { { Text(text = item) } } else null,
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                )
            }
        }
    }
}

@Preview
@Composable
fun BanterboxNavigationRailPreview() {
    val items = listOf("Home", "Search", "Interests")
    val icons = listOf(
        BanterboxSellerIcons.UpcomingBorder,
        BanterboxSellerIcons.BookmarksBorder,
        BanterboxSellerIcons.Grid3x3,
    )
    val selectedIcons = listOf(
        BanterboxSellerIcons.Upcoming,
        BanterboxSellerIcons.Bookmarks,
        BanterboxSellerIcons.Grid3x3,
    )

    BanterboxTheme {
        BanterboxNavigationRail {
            items.forEachIndexed { index, item ->
                BanterboxNavigationRailItem(
                    icon = {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            imageVector = selectedIcons[index],
                            contentDescription = item,
                        )
                    },
                    label = { Text(item) },
                    selected = index == 0,
                    onClick = { },
                )
            }
        }
    }
}
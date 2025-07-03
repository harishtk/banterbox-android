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
import space.banterbox.app.core.designsystem.ShopsSellerIcons
import space.banterbox.app.core.designsystem.component.navigation.DefaultNavigationBar
import space.banterbox.app.core.designsystem.component.navigation.DefaultNavigationBarItem
import space.banterbox.app.core.designsystem.component.navigation.ShopsNavigationBarItemDefaults
import space.banterbox.app.ui.theme.NavigationBarBackground
import space.banterbox.app.ui.theme.BanterboxTheme

@Composable
fun RowScope.ShopsNavigationBarItem(
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
        colors = ShopsNavigationBarItemDefaults.colors()
    )
}

/**
 * Shops navigation bar with content slot. Wraps Material 3 [NavigationBar].
 *
 * @param modifier Modifier to be applied to the navigation bar.
 * @param content Destinations inside the navigation bar. This should contain multiple
 * [NavigationBarItem]s.
 */
@Composable
fun ShopsNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    DefaultNavigationBar(
        modifier = modifier,
        content = content,
    )
}

/**
 * Shops navigation rail item with icon and label content slots. Wraps Material 3
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
fun ShopsNavigationRailItem(
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
            selectedIconColor = ShopsNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = ShopsNavigationDefaults.navigationContentColor(),
            selectedTextColor = ShopsNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = ShopsNavigationDefaults.navigationContentColor(),
            indicatorColor = ShopsNavigationDefaults.navigationIndicatorColor(),
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
fun ShopsNavigationRail(
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    NavigationRail(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = ShopsNavigationDefaults.navigationContentColor(),
        header = header,
        content = content,
    )
}

/**
 * Shops navigation default values.
 */
object ShopsNavigationDefaults {

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
fun ShopsNavigationPreview() {
    val items = listOf("Home", "Insights", null, "Inventory", "Admin")
    val icons = listOf(
        ShopsSellerIcons.Id_Home_Outline,
        ShopsSellerIcons.Id_Insights_Outline,
        ShopsSellerIcons.Id_New,
        ShopsSellerIcons.Id_Inventory_Outline,
        ShopsSellerIcons.Id_Admin_Outline,
    )
    val selectedIcons = listOf(
        ShopsSellerIcons.Id_Home_Filled,
        ShopsSellerIcons.Id_Insights_Filled,
        ShopsSellerIcons.Id_New,
        ShopsSellerIcons.Id_Inventory_Filled,
        ShopsSellerIcons.Id_Admin_Filled,
    )

    var selectedIndex by remember { mutableIntStateOf(0) }

    BanterboxTheme {
        ShopsNavigationBar {
            items.forEachIndexed { index, item ->
                ShopsNavigationBarItem(
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
fun ShopsNavigationRailPreview() {
    val items = listOf("Home", "Search", "Interests")
    val icons = listOf(
        ShopsSellerIcons.UpcomingBorder,
        ShopsSellerIcons.BookmarksBorder,
        ShopsSellerIcons.Grid3x3,
    )
    val selectedIcons = listOf(
        ShopsSellerIcons.Upcoming,
        ShopsSellerIcons.Bookmarks,
        ShopsSellerIcons.Grid3x3,
    )

    BanterboxTheme {
        ShopsNavigationRail {
            items.forEachIndexed { index, item ->
                ShopsNavigationRailItem(
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
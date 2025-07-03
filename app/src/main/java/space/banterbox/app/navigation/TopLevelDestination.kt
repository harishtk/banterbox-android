package space.banterbox.app.navigation

import space.banterbox.app.R
import space.banterbox.app.core.designsystem.ShopsSellerIcons

/**
 * Type for top level destinations in the application. Each of these destinations
 * can contain one or more screens (based on the window size). Navigation from one screen to the
 * next within a single destination will be handled directly in composables.
 */
enum class TopLevelDestination(
    val selectedIcon: Int,
    val unselectedIcon: Int,
    val iconTextId: Int,
    val titleTextId: Int?,
) {
    HOME(
        selectedIcon = ShopsSellerIcons.Id_Home_Filled,
        unselectedIcon = ShopsSellerIcons.Id_Home_Outline,
        iconTextId = R.string.home,
        titleTextId = R.string.home,
    ),
    INSIGHTS(
        selectedIcon = ShopsSellerIcons.Id_Insights_Filled,
        unselectedIcon = ShopsSellerIcons.Id_Insights_Outline,
        iconTextId = R.string.insights,
        titleTextId = R.string.insights,
    ),
    CREATE(
        selectedIcon = ShopsSellerIcons.Id_New,
        unselectedIcon = ShopsSellerIcons.Id_New,
        iconTextId = R.string.home,
        titleTextId = null,
    ),
    INVENTORY(
        selectedIcon = ShopsSellerIcons.Id_Inventory_Filled,
        unselectedIcon = ShopsSellerIcons.Id_Inventory_Outline,
        iconTextId = R.string.inventory,
        titleTextId = R.string.inventory,
    ),
    ADMIN(
        selectedIcon = ShopsSellerIcons.Id_Admin_Filled,
        unselectedIcon = ShopsSellerIcons.Id_Admin_Outline,
        iconTextId = R.string.admin,
        titleTextId = R.string.admin,
    ),
}
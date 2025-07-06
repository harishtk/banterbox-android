package space.banterbox.app.navigation

import space.banterbox.app.R
import space.banterbox.app.core.designsystem.BanterboxSellerIcons

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
        selectedIcon = BanterboxSellerIcons.Id_Home_Filled,
        unselectedIcon = BanterboxSellerIcons.Id_Home_Outline,
        iconTextId = R.string.home,
        titleTextId = R.string.home,
    ),
    PROFILE(
        selectedIcon = BanterboxSellerIcons.Id_Admin_Outline,
        unselectedIcon = BanterboxSellerIcons.Id_Admin_Outline,
        iconTextId = R.string.profile,
        titleTextId = R.string.profile,
    ),
}
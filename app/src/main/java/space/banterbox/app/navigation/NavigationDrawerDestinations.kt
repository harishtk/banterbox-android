package space.banterbox.app.navigation

import space.banterbox.app.R
import space.banterbox.app.core.designsystem.BanterboxSellerIcons

enum class NavigationDrawerDestination(
    val selectedIcon: Int,
    val unselectedIcon: Int,
    val iconTextId: Int,
    val labelTextId: Int,
) {
    Support(
        selectedIcon = BanterboxSellerIcons.Id_Help_Outline,
        unselectedIcon = BanterboxSellerIcons.Id_Help_Outline,
        iconTextId = R.string.support,
        labelTextId = R.string.support
    ),
    Settings(
        selectedIcon = BanterboxSellerIcons.Id_SettingsGear_Outline,
        unselectedIcon = BanterboxSellerIcons.Id_SettingsGear_Outline,
        iconTextId = R.string.settings,
        labelTextId = R.string.settings
    ),
}
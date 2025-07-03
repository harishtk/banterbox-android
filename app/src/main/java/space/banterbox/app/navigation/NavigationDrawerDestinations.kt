package space.banterbox.app.navigation

import space.banterbox.app.R
import space.banterbox.app.core.designsystem.ShopsSellerIcons

enum class NavigationDrawerDestination(
    val selectedIcon: Int,
    val unselectedIcon: Int,
    val iconTextId: Int,
    val labelTextId: Int,
) {
    MyStore(
        selectedIcon = ShopsSellerIcons.Id_Home_Alt,
        unselectedIcon = ShopsSellerIcons.Id_Home_Alt,
        iconTextId = R.string.my_store,
        labelTextId = R.string.my_store
    ),
    Website(
        selectedIcon = ShopsSellerIcons.Id_Website_Outline,
        unselectedIcon = ShopsSellerIcons.Id_Website_Outline,
        iconTextId = R.string.website,
        labelTextId = R.string.website
    ),
    Customers(
        selectedIcon = ShopsSellerIcons.Id_Group_Outline,
        unselectedIcon = ShopsSellerIcons.Id_Group_Outline,
        iconTextId = R.string.customers,
        labelTextId = R.string.customers
    ),
    Campaigns(
        selectedIcon = ShopsSellerIcons.Id_GlowingBulb_Outline,
        unselectedIcon = ShopsSellerIcons.Id_GlowingBulb_Outline,
        iconTextId = R.string.campaigns,
        labelTextId = R.string.campaigns
    ),
    Orders(
        selectedIcon = ShopsSellerIcons.Id_ClockFive,
        unselectedIcon = ShopsSellerIcons.Id_ClockFive,
        iconTextId = R.string.orders,
        labelTextId = R.string.orders
    ),
    Offers(
        selectedIcon = ShopsSellerIcons.Id_Percent,
        unselectedIcon = ShopsSellerIcons.Id_Percent,
        iconTextId = R.string.offers,
        labelTextId = R.string.offers
    ),
    Revenue(
        selectedIcon = ShopsSellerIcons.Id_MoneyBill_Outline,
        unselectedIcon = ShopsSellerIcons.Id_MoneyBill_Outline,
        iconTextId = R.string.revenue,
        labelTextId = R.string.revenue
    ),
    Analytics(
        selectedIcon = ShopsSellerIcons.Id_Bell_Outline,
        unselectedIcon = ShopsSellerIcons.Id_Bell_Outline,
        iconTextId = R.string.analytics,
        labelTextId = R.string.analytics
    ),
    Support(
        selectedIcon = ShopsSellerIcons.Id_Help_Outline,
        unselectedIcon = ShopsSellerIcons.Id_Help_Outline,
        iconTextId = R.string.support,
        labelTextId = R.string.support
    ),
    Settings(
        selectedIcon = ShopsSellerIcons.Id_SettingsGear_Outline,
        unselectedIcon = ShopsSellerIcons.Id_SettingsGear_Outline,
        iconTextId = R.string.settings,
        labelTextId = R.string.settings
    ),
}
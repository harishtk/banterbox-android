package space.banterbox.core.datastore

import space.banterbox.core.datastore.model.DarkThemeConfig
import space.banterbox.core.datastore.model.ThemeBrand

data class UserData(
    val userId: String,
    val username: String,
    val profileName: String,
    val profileImage: String,
    val unreadNotificationCount: Int = 0,
    val shouldUpdateProfileOnce: Boolean = false,
    val onboardStep: String = "",
    val serverUnderMaintenance: Boolean = false,
    val lastGreetedTime: Long = 0,
    val shouldShowAppRating: Boolean = false,
    val themeBrand: ThemeBrand = ThemeBrand.DEFAULT,
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.LIGHT,
    val useDynamicColor: Boolean = true,
    val isAppRatingShownAtLeastOnce: Boolean = false,
)
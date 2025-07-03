package com.pepul.shops.core.datastore

import com.pepul.shops.core.datastore.model.DarkThemeConfig
import com.pepul.shops.core.datastore.model.ThemeBrand

data class UserData(
    val userId: String,
    val pinCode: String,
    val area: String,
    val profileName: String,
    val profileImage: String,
    val profileThumb: String,
    val cartCount: Int = 0,
    val unreadNotificationCount: Int = 0,
    val isMuted: Boolean = false,
    val shouldUpdateProfileOnce: Boolean = false,
    val onboardStep: String = "",
    val serverUnderMaintenance: Boolean = false,
    val lastGreetedTime: Long = 0,
    val shouldShowAppRating: Boolean = false,
    val themeBrand: ThemeBrand = ThemeBrand.DEFAULT,
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.LIGHT,
    val useDynamicColor: Boolean = false,
    val isAppRatingShownAtLeastOnce: Boolean = false,
    val userRole: String = "",
)
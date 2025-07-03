package space.banterbox.app.core.domain.repository

import com.pepul.shops.core.datastore.UserData
import com.pepul.shops.core.datastore.model.DarkThemeConfig
import com.pepul.shops.core.datastore.model.ThemeBrand
import space.banterbox.app.core.domain.model.LoginUser
import space.banterbox.app.feature.onboard.presentation.components.OnboardStep
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {

    /**
     * Stream of [UserData]
     */
    val userData: Flow<UserData>

    /**
     * Set's the [LoginUser] of the currently logged user.
     */
    suspend fun setUserData(userData: LoginUser?)

    suspend fun updateUnreadNotificationCount(count: Int)

    suspend fun updateUserPinCode(pinCode: String, areaName: String)

    suspend fun updateProfileName(name: String)

    suspend fun updateProfileThumb(imageName: String)

    suspend fun updateProfileImage(imageName: String)

    suspend fun updateCartCount(count: Int)

    suspend fun updateVideoMuteStatus(muted: Boolean)

    suspend fun setShouldUpdateProfileOnce(shouldUpdate: Boolean)

    suspend fun setServerUnderMaintenance(underMaintenance: Boolean)

    suspend fun setLastGreetedTime(timestamp: Long)

    suspend fun setShowAppRating(show: Boolean)

    suspend fun updateOnboardStep(onboardStep: String)

    /**
     * Sets the desired theme brand.
     */
    suspend fun setThemeBrand(themeBrand: ThemeBrand)

    /**
     * Sets the desired dark theme config.
     */
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    /**
     * Sets the preferred dynamic color config.
     */
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    // TODO: Add other keys here.
}
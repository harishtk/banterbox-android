package com.pepul.shops.core.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pepul.shops.core.datastore.di.DataStoreType
import com.pepul.shops.core.datastore.di.BanterboxDataStoreType
import com.pepul.shops.core.datastore.model.DarkThemeConfig
import com.pepul.shops.core.datastore.model.ThemeBrand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PsPreferencesDataSource @Inject constructor(
    @param:DataStoreType(BanterboxDataStoreType.User)
    private val userPreferencesStore: DataStore<Preferences>,
) {

    val userData: Flow<UserData> = userPreferencesStore.data
        .catch { t ->
            // datastore data throws an IOException when an error is encountered when reading data
            if (t is IOException) {
                emit(emptyPreferences())
            } else {
                throw t
            }
        }.map { preferences ->
            UserData(
                userId = preferences[UserPreferenceKeys.UserId] ?: "",
                pinCode = preferences[UserPreferenceKeys.PinCode] ?: "",
                area = preferences[UserPreferenceKeys.AreaName] ?: "",
                profileName = preferences[UserPreferenceKeys.ProfileName] ?: "",
                profileImage = preferences[UserPreferenceKeys.ProfileImage] ?: "",
                profileThumb = preferences[UserPreferenceKeys.ProfileThumb] ?: "",
                cartCount = preferences[UserPreferenceKeys.CartCount] ?: 0,
                unreadNotificationCount = preferences[UserPreferenceKeys.UnReadNotificationCount] ?: 0,
                isMuted = preferences[UserPreferenceKeys.AudioMuted] ?: false,
                shouldUpdateProfileOnce = preferences[UserPreferenceKeys.ShouldUpdateProfileOnce] ?: false,
                serverUnderMaintenance = preferences[UserPreferenceKeys.ServerUnderMaintenance] ?: false,
                lastGreetedTime = preferences[UserPreferenceKeys.LastGreetedTime] ?: 0,
                shouldShowAppRating = preferences[UserPreferenceKeys.ShouldShowAppRating] ?: false,
                themeBrand = getThemeBrand(preferences),
                darkThemeConfig = getDarkThemeConfig(preferences),
                useDynamicColor = preferences[UserPreferenceKeys.UseDynamicColor] ?: false,
                isAppRatingShownAtLeastOnce = preferences[UserPreferenceKeys.AppRatingShownAtLeastOnce] ?: false,
                onboardStep = preferences[UserPreferenceKeys.OnboardStep] ?: "",
            )
        }

    suspend fun setUserId(userId: String) {
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.UserId] = userId
        }
    }

    suspend fun setPinCode(pinCode: String) {
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.PinCode] = pinCode
        }
    }

    suspend fun setArea(areaName: String) {
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.AreaName] = areaName
        }
    }

    suspend fun setProfileName(name: String) {
        Log.d("DataStore", "setProfileName() called with: name = $name")
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.ProfileName] = name
        }
    }

    suspend fun setProfileImage(imageName: String) {
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.ProfileImage] = imageName
        }
    }

    suspend fun setProfileThumb(imageName: String) {
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.ProfileThumb] = imageName
        }
    }

    suspend fun setUnreadNotificationCount(count: Int) {
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.UnReadNotificationCount] = count
        }
    }

    suspend fun setAudioMuted(muted: Boolean) {
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.AudioMuted] = muted
        }
    }

    suspend fun updateCartCount(count: Int) {
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.CartCount] = count
        }
    }

    suspend fun setShouldUpdateProfileOnce(shouldUpdate: Boolean) {
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.ShouldUpdateProfileOnce] = shouldUpdate
        }
    }

    suspend fun setServerUnderMaintenance(underMaintenance: Boolean) {
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.ServerUnderMaintenance] = underMaintenance
        }
    }

    suspend fun setLastGreetedTime(timestamp: Long) {
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.LastGreetedTime] = timestamp
        }
    }

    suspend fun setShowAppRating(show: Boolean) {
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.ShouldShowAppRating] = show
            if (show) {
                preferences[UserPreferenceKeys.AppRatingShownAtLeastOnce] = true
            }
        }
    }

    suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        userPreferencesStore.edit { preferences ->
            val themeBrandValue = when (themeBrand) {
                ThemeBrand.DEFAULT -> THEME_BRAND_DEFAULT
                ThemeBrand.ANDROID -> THEME_BRAND_ANDROID
            }
            preferences[UserPreferenceKeys.ThemeBrand] = themeBrandValue
        }
    }

    suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        userPreferencesStore.edit { preferences ->
               preferences[UserPreferenceKeys.UseDynamicColor] = useDynamicColor
        }
    }

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        userPreferencesStore.edit { preferences ->
            val darkModeConfigValue = when (darkThemeConfig) {
                DarkThemeConfig.FOLLOW_SYSTEM -> DARK_THEME_CONFIG_FOLLOW_SYSTEM
                DarkThemeConfig.LIGHT -> DARK_THEME_CONFIG_LIGHT
                DarkThemeConfig.DARK -> DARK_THEME_CONFIG_DARK
            }
            preferences[UserPreferenceKeys.DarkThemeConfig] = darkModeConfigValue
        }
    }

    suspend fun setOnboardStep(onboardStep: String) {
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.OnboardStep] = onboardStep
        }
    }

    suspend fun logoutUser() {
        userPreferencesStore.edit { preferences ->
            preferences[UserPreferenceKeys.UserId] = ""
            preferences[UserPreferenceKeys.PinCode] = ""
            preferences[UserPreferenceKeys.ProfileName] = ""
            preferences[UserPreferenceKeys.ProfileImage] = ""
            preferences[UserPreferenceKeys.ProfileThumb] = ""
            preferences[UserPreferenceKeys.CartCount] = 0
            preferences[UserPreferenceKeys.UnReadNotificationCount] = 0
            preferences[UserPreferenceKeys.AudioMuted] = false
            preferences[UserPreferenceKeys.ShouldUpdateProfileOnce] = false
            preferences[UserPreferenceKeys.LastGreetedTime] = 0
            preferences[UserPreferenceKeys.ShouldShowAppRating] = false
            preferences[UserPreferenceKeys.UseDynamicColor] = false
            preferences[UserPreferenceKeys.ThemeBrand] = THEME_BRAND_DEFAULT
            preferences[UserPreferenceKeys.DarkThemeConfig] = DARK_THEME_CONFIG_FOLLOW_SYSTEM
            preferences[UserPreferenceKeys.OnboardStep] = ""
        }
    }

    suspend fun clearAll() {
        userPreferencesStore.edit { preferences -> preferences.clear() }
    }

    private fun getThemeBrand(preferences: Preferences): ThemeBrand {
        return when (preferences[UserPreferenceKeys.ThemeBrand]) {
            THEME_BRAND_DEFAULT -> ThemeBrand.DEFAULT
            THEME_BRAND_ANDROID -> ThemeBrand.ANDROID
            else -> ThemeBrand.DEFAULT
        }
    }

    private fun getDarkThemeConfig(preferences: Preferences): DarkThemeConfig {
        return when (preferences[UserPreferenceKeys.DarkThemeConfig]) {
            DARK_THEME_CONFIG_DARK -> DarkThemeConfig.DARK
            DARK_THEME_CONFIG_LIGHT -> DarkThemeConfig.LIGHT
            DARK_THEME_CONFIG_FOLLOW_SYSTEM -> DarkThemeConfig.FOLLOW_SYSTEM
            else -> DarkThemeConfig.LIGHT
        }
    }

    object UserPreferenceKeys {
        val UserId = stringPreferencesKey("user_id")
        val PinCode = stringPreferencesKey("pin_code")
        val AreaName = stringPreferencesKey("area_name")
        val ProfileName = stringPreferencesKey("profile_name")
        val ProfileImage = stringPreferencesKey("profile_image")
        val ProfileThumb = stringPreferencesKey("profile_thumb")
        val CartCount = intPreferencesKey("cart_count")
        val UnReadNotificationCount = intPreferencesKey("unread_notification_count")
        val AudioMuted = booleanPreferencesKey("is_audio_muted")

        /* Theme based flags */
        val ThemeBrand = intPreferencesKey("theme_brand")
        val UseDynamicColor = booleanPreferencesKey("theme_use_dynamic_color")
        val DarkThemeConfig = intPreferencesKey("theme_dark_mode_config")

        /* App flow flags */
        val ShouldShowAppRating = booleanPreferencesKey("should_show_app_rating")
        val AppRatingShownAtLeastOnce = booleanPreferencesKey("app_rating_shown_at_least_once")

        /* Redirection based flags */
        val ShouldUpdateProfileOnce = booleanPreferencesKey("should_update_profile_once")
        val ServerUnderMaintenance = booleanPreferencesKey("server_under_maintenance")
        val LastGreetedTime = longPreferencesKey("last_greeted_time")
        val OnboardStep = stringPreferencesKey("onboard_step")
    }

    companion object {
        private const val DARK_THEME_CONFIG_FOLLOW_SYSTEM = 0
        private const val DARK_THEME_CONFIG_LIGHT = 1
        private const val DARK_THEME_CONFIG_DARK = 2

        private const val THEME_BRAND_DEFAULT = 0
        private const val THEME_BRAND_ANDROID = 1
    }
}


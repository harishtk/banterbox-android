package space.banterbox.app.core.data.repository

import com.pepul.shops.core.analytics.AnalyticsLogger
import com.pepul.shops.core.common.concurrent.AiaDispatchers
import com.pepul.shops.core.common.concurrent.Dispatcher
import com.pepul.shops.core.datastore.PsPreferencesDataSource
import com.pepul.shops.core.datastore.UserData
import com.pepul.shops.core.datastore.model.DarkThemeConfig
import com.pepul.shops.core.datastore.model.ThemeBrand
import space.banterbox.app.common.util.logging.Log
import space.banterbox.app.core.domain.model.LoginUser
import space.banterbox.app.core.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultUserDataRepository @Inject constructor(
    private val psPreferencesDataSource: PsPreferencesDataSource,
    private val analyticsLogger: AnalyticsLogger,
    @Dispatcher(AiaDispatchers.Io)
    private val dispatcher: CoroutineDispatcher,
) : UserDataRepository {

    override val userData: Flow<UserData> =
        psPreferencesDataSource.userData

    override suspend fun setUserData(userData: LoginUser?) = withContext(dispatcher) {
        Timber.d("setUserData() called with userData=$userData")
        if (userData == null) {
            psPreferencesDataSource.clearAll()
        } else {
            with(psPreferencesDataSource) {
                psPreferencesDataSource.setUserId(userData.userId)
                psPreferencesDataSource.setProfileName(userData.profileName)
                psPreferencesDataSource.setProfileThumb(userData.profileThumb)
                setProfileImage(userData.profileImage)
                psPreferencesDataSource.setUnreadNotificationCount(userData.notificationCount)
                psPreferencesDataSource.setOnboardStep(userData.onboardStep)
            }
        }
    }

    override suspend fun updateUnreadNotificationCount(count: Int) {
        psPreferencesDataSource.setUnreadNotificationCount(count)
    }

    override suspend fun updateUserPinCode(pinCode: String, areaName: String) = withContext(dispatcher) {
        psPreferencesDataSource.setPinCode(pinCode)
        psPreferencesDataSource.setArea(areaName)
    }

    override suspend fun updateProfileName(name: String) = withContext(dispatcher) {
        psPreferencesDataSource.setProfileName(name)
    }

    override suspend fun updateProfileImage(imageName: String) = withContext(dispatcher) {
        psPreferencesDataSource.setProfileImage(imageName)
    }

    override suspend fun updateProfileThumb(imageName: String) = withContext(dispatcher) {
        psPreferencesDataSource.setProfileThumb(imageName)
    }

    override suspend fun updateCartCount(count: Int) {
        psPreferencesDataSource.updateCartCount(count)
    }

    override suspend fun updateVideoMuteStatus(muted: Boolean) {
        psPreferencesDataSource.setAudioMuted(muted)
    }

    override suspend fun setShouldUpdateProfileOnce(shouldUpdate: Boolean) {
        psPreferencesDataSource.setShouldUpdateProfileOnce(shouldUpdate)
    }

    override suspend fun setServerUnderMaintenance(underMaintenance: Boolean) {
        psPreferencesDataSource.setServerUnderMaintenance(underMaintenance)
    }

    override suspend fun setLastGreetedTime(timestamp: Long) {
        psPreferencesDataSource.setLastGreetedTime(timestamp)
    }

    override suspend fun setShowAppRating(show: Boolean) {
        psPreferencesDataSource.setShowAppRating(show)
    }

    override suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        psPreferencesDataSource.setThemeBrand(themeBrand)
    }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        psPreferencesDataSource.setDarkThemeConfig(darkThemeConfig)
    }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        psPreferencesDataSource.setDynamicColorPreference(useDynamicColor)
    }

    override suspend fun updateOnboardStep(onboardStep: String) {
        psPreferencesDataSource.setOnboardStep(onboardStep)
    }
}
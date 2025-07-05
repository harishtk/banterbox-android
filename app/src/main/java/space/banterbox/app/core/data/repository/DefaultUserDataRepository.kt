package space.banterbox.app.core.data.repository

import space.banterbox.core.analytics.AnalyticsLogger
import space.banterbox.core.common.concurrent.AiaDispatchers
import space.banterbox.core.common.concurrent.Dispatcher
import space.banterbox.core.datastore.PsPreferencesDataSource
import space.banterbox.core.datastore.UserData
import space.banterbox.core.datastore.model.DarkThemeConfig
import space.banterbox.core.datastore.model.ThemeBrand
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
    @param:Dispatcher(AiaDispatchers.Io)
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
                psPreferencesDataSource.setUsername(userData.username)
                psPreferencesDataSource.setDisplayName(userData.profileName)
                setProfileImage(userData.profileImage)
                psPreferencesDataSource.setUnreadNotificationCount(userData.notificationCount)
                psPreferencesDataSource.setOnboardStep(userData.onboardStep)
            }
        }
    }

    override suspend fun updateUnreadNotificationCount(count: Int) {
        psPreferencesDataSource.setUnreadNotificationCount(count)
    }

    override suspend fun updateProfileName(name: String) = withContext(dispatcher) {
        psPreferencesDataSource.setDisplayName(name)
    }

    override suspend fun updateProfileImage(imageName: String) = withContext(dispatcher) {
        psPreferencesDataSource.setProfileImage(imageName)
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
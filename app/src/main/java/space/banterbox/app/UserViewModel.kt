package space.banterbox.app

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepul.shops.core.datastore.PsPreferencesDataSource
import space.banterbox.app.core.di.AppDependencies
import space.banterbox.app.core.domain.repository.UserDataRepository
import space.banterbox.app.core.domain.usecase.AuthenticationState
import space.banterbox.app.core.domain.usecase.UserAuthStateUseCase
import space.banterbox.app.core.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.CancellationException
import javax.inject.Inject

/**
 * TODO: 1. Add accounts repository
 * TODO: 2. Handle database.
 * TODO: 3. Handle auto login.
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    networkMonitor: NetworkMonitor,
    userAuthStateUseCase: UserAuthStateUseCase,
    private val userDataRepository: UserDataRepository,
    // private val accountsRepository: AccountsRepository,
    private val dataStore: PsPreferencesDataSource,
    // private val database: AppDatabase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val shouldAutoLogin = MutableStateFlow(true)

    /**
     * Exposes a [UserData] cold flow
     */
    val userDataFlow = userDataRepository.userData
        .distinctUntilChanged()

    /**
     * Holds the current authentication state of the user.
     */
    val authenticationState = userAuthStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthenticationState.UNKNOWN
        )

    private var autoLoginJob: Job? = null

    init {
        Timber.d("Initializing view model..")
        val isOnlineFlow = networkMonitor.isOnline
            .distinctUntilChanged()
        val authenticatedFlow = authenticationState
            .map { it.isAuthenticated() }
            .distinctUntilChanged()

        combine(
            combine(
                isOnlineFlow,
                shouldAutoLogin,
                Boolean::and
            ),
            authenticatedFlow,
            Boolean::and
        ).map {
            if (it) {
                autoLogin()
            }
        }
            .launchIn(viewModelScope)

        /*combine(
            isOnlineFlow,
            shouldAutoLogin,
            Boolean::and
        ).map {
            if (it) {
                autoLogin()
            }
        }
            .launchIn(viewModelScope)*/
    }

    private fun autoLogin() {
        autoLoginJob?.cancel(CancellationException("New Request"))
        /*val autoLoginRequest = AutoLoginRequest(System.currentTimeMillis())
        autoLoginJob = viewModelScope.launch {
            accountsRepository.autoLogin(autoLoginRequest).collectLatest { result ->
                Timber.d("Auto Login: $result")
                when (result) {
                    is Result.Loading -> Noop()
                    is Result.Error -> {
                         // TODO: Retry?
                    }
                    is Result.Success -> {
                        // TODO: -partially_done- parse result
                        _forceUpdate.update { result.data.forceUpdate }
                        setShouldAutoLogin(false)
                        result.data.loginUser?.let { loginUser ->
                            userDataRepository.setUserData(loginUser)
                        }
                        userDataRepository.setServerUnderMaintenance(result.data.maintenance)
                    }
                }
            }
        }*/
    }

    /* Force update flag */
    private var _forceUpdate: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val forceUpdate = _forceUpdate
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val serverMaintenance = userDataFlow.map { it.serverUnderMaintenance }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    /* Cart count */
    val cartCount: StateFlow<Int> = userDataRepository.userData
        .map { it.cartCount }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    fun setCartCount(count: Int) {
        viewModelScope.launch { userDataRepository.updateCartCount(count) }
    }

    /* Notification badge count */
    val notificationBadgeCount: StateFlow<Int> = userDataRepository.userData
        .map { it.unreadNotificationCount }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    fun setNotificationBadgeCount(count: Int) = viewModelScope.launch {
        userDataRepository.updateUnreadNotificationCount(count)
    }

    fun setLastGreetedTime(timestamp: Long) {
        viewModelScope.launch { userDataRepository.setLastGreetedTime(timestamp) }
    }

    fun setShowAppRating(show: Boolean) {
        viewModelScope.launch { userDataRepository.setShowAppRating(show) }
    }

    fun logout(cont: () -> Unit = {}) = viewModelScope.launch(Dispatchers.IO) {
        val userId = userDataRepository.userData.firstOrNull()?.userId ?: ""
        // database.clearAllTables()
        userDataRepository.setUserData(null) /* Logs out the user */
        // accountsRepository.logout(LogoutRequest(userId))
        AppDependencies.persistentStore?.logout()
        cont()
    }

    fun setShouldAutoLogin(shouldLogin: Boolean) {
        shouldAutoLogin.update { shouldLogin }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("view model destroyed.")
    }
}
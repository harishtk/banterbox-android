package space.banterbox.app.feature.onboard.presentation.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.banterbox.app.Constant
import space.banterbox.app.R
import space.banterbox.app.common.util.ResolvableException
import space.banterbox.app.common.util.UiText
import space.banterbox.app.common.util.loadstate.LoadState
import space.banterbox.app.common.util.loadstate.LoadStates
import space.banterbox.app.common.util.loadstate.LoadType
import space.banterbox.app.core.Noop
import space.banterbox.app.core.di.AppDependencies
import space.banterbox.app.core.domain.repository.UserDataRepository
import space.banterbox.app.core.net.ApiException
import space.banterbox.app.core.net.NoInternetException
import space.banterbox.app.core.persistence.PersistentStore
import space.banterbox.app.core.util.ErrorMessage
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.onboard.domain.model.LoginData
import space.banterbox.app.feature.onboard.domain.model.request.LoginRequest
import space.banterbox.app.feature.onboard.domain.repository.AuthRepository
import space.banterbox.app.feature.onboard.presentation.util.InvalidUsernameException
import space.banterbox.app.feature.onboard.presentation.util.LoginException
import space.banterbox.app.feature.onboard.presentation.util.RecaptchaException
import space.banterbox.app.nullAsEmpty
import space.banterbox.core.analytics.Analytics
import space.banterbox.core.analytics.AnalyticsLogger
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userDataRepository: UserDataRepository,
    private val persistentStore: PersistentStore,
    private val analyticsLogger: AnalyticsLogger,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val viewModelState = MutableStateFlow<LoginViewModelState>(LoginViewModelState())
    val uiState = viewModelState
        .asStateFlow()

    private val _uiEvent = MutableSharedFlow<LoginUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (LoginUiAction) -> Unit

    private val actionStream = MutableSharedFlow<LoginUiAction>()

    private var signupJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }

        val toggleButtonState: Boolean = savedStateHandle[TOGGLE_BUTTON_STATE] ?: false
        viewModelState.update { it.copy(toggleButtonState = toggleButtonState) }

        val typedUsername = savedStateHandle[TYPED_USERNAME] ?: ""
        viewModelState.update { it.copy(typedUsername = typedUsername) }

        actionStream
            .filterIsInstance<LoginUiAction.TypingUsername>()
            .distinctUntilChanged()
            .onEach { action ->
                viewModelState.update { it.copy(typedUsername = action.typedUsername.trim()) }
            }
            .launchIn(viewModelScope)

        actionStream
            .filterIsInstance<LoginUiAction.TypingPassword>()
            .distinctUntilChanged()
            .onEach { action ->
                viewModelState.update { it.copy(typedPassword = action.typedPassword.trim()) }
            }
            .launchIn(viewModelScope)
    }

    private fun onUiAction(action: LoginUiAction) {
        when (action) {
            is LoginUiAction.ErrorShown -> {
                popError(action.id)
            }
            is LoginUiAction.ToggleConsentButton -> {
                viewModelState.update { it.copy(toggleButtonState = action.checked) }
            }
            is LoginUiAction.TypingUsername,
            is LoginUiAction.TypingPassword -> {
                viewModelScope.launch { actionStream.emit(action) }
            }
            is LoginUiAction.Validate -> {
                validateInternal(action.suppressError, action.isDeletedAccountRetrieve)
                // setDeleteAccountRetrieveInternal(action.isDeletedAccountRetrieve)
            }
            LoginUiAction.ResetLoading -> {
                setLoading(LoadType.ACTION, LoadState.NotLoading.InComplete)
            }
        }
    }

    private fun validateInternal(suppressError: Boolean = false, isDeletedAccountRetrieve: Boolean = false) {

        // toggle button validation
        /*if (!uiState.value.toggleButtonState) {
            val cause = TermsAndPrivacyException()
            addError(
                t = ResolvableException(cause),
                uiText = UiText.StringResource(R.string.read_and_accept_terms)
            )
            return
        }*/

        // Username validation
        val username = viewModelState.value.typedUsername
        if (username.isBlank()) {
            val cause = InvalidUsernameException("Enter a valid username")
            addError(
                t = ResolvableException(cause),
                uiText = UiText.StringResource(R.string.enter_a_valid_username)
            )
            return
        }

        // toggle button validation
        if (!viewModelState.value.toggleButtonState) {
            val cause = IllegalStateException("Consent button is not checked")
            addError(
                t = ResolvableException(cause),
                uiText = UiText.StringResource(R.string.read_and_accept_terms)
            )
            return
        }

        signInApiInternal()
    }

    private fun signInApiInternal() {
        val username = viewModelState.value.typedUsername
        val password = viewModelState.value.typedPassword

        val request = LoginRequest(
            username = username,
            password = password
        )
        login(request)
    }

    private fun login(request: LoginRequest) {
        if (signupJob?.isActive == true) {
            Timber.w("Signup request aborted. Already active.")
            return
        }
        signupJob?.cancel()
        setLoading(LoadType.ACTION, LoadState.Loading())
        signupJob = viewModelScope.launch {
            when (val result = authRepository.login(request)) {
                Result.Loading -> Noop()
                is Result.Error -> {
                    when (result.exception) {
                        is ApiException -> {
                            when (result.exception.cause) {
                                is RecaptchaException -> {
                                    addError(
                                        t = result.exception,
                                        uiText = UiText.somethingWentWrong
                                    )
                                }

                                is LoginException -> {
                                    addError(
                                        t = result.exception,
                                        uiText = UiText.DynamicString("Invalid username or password")
                                    )
                                }
                                else -> {
                                    addError(
                                        t = result.exception,
                                        uiText = UiText.somethingWentWrong
                                    )
                                }
                            }
                        }
                        is NoInternetException -> {
                            addError(
                                t = result.exception,
                                uiText = UiText.noInternet
                            )
                        }
                    }
                    setLoading(LoadType.ACTION, LoadState.Error(result.exception))
                }
                is Result.Success -> {
                    setLoading(LoadType.ACTION, LoadState.NotLoading.Complete)

                    /* CAUTION: ** order of invocation matters to satisfy the reactive conditions ** */
                    AppDependencies.persistentStore?.apply {
                        setFcmTokenSynced(true)
                        setLastTokenSyncTime(System.currentTimeMillis())
                        setInstallReferrerFetched(false)
                        // setTempId(result.data.loginUser?.userId)
                    }
                    // TODO: hardcoded value
                    userDataRepository.setShouldUpdateProfileOnce(false)
                    setLoginData(result.data)
                    analyticsLogger.logEvent(Analytics.Event.ONBOARD_SUCCESS_EVENT)
                    sendEvent(LoginUiEvent.ShowToast(UiText.DynamicString("Login successful")))
                }
            }
        }
    }

    fun resetOtpSent() {
        viewModelState.update { it.copy(isLoginSuccessful = false) }
    }

    fun handleBackPressed(): Boolean {
        return false
    }

    private fun setLoading(
        loadType: LoadType,
        loadState: LoadState
    ) {
        val newLoadState = viewModelState.value.loadState.modifyState(loadType, loadState)
        viewModelState.update { it.copy(loadState = newLoadState) }
    }

    private fun addError(
        t: Throwable,
        uiText: UiText
    ) {
        /*ErrorMessage(
            id = System.currentTimeMillis(),
            exception = t,
            message = uiText
        ).also { errorMessage ->
            val errors = viewModelState.value.errors.toMutableList().apply {
                add(errorMessage)
            }
            viewModelState.update { it.copy(errors = errors) }
        }*/
        viewModelState.update { state ->
            state.copy(
                exception = t,
                uiErrorMessage = uiText
            )
        }
    }

    private fun popError(id: Long) {
        /*val errors = viewModelState.value.errors.filterNot { it.id == id }
        viewModelState.update { it.copy(errors = errors) }*/
        val newLoadState = uiState.value.loadState.modifyState(LoadType.ACTION, LoadState.NotLoading.InComplete)
        viewModelState.update { state ->
            state.copy(
                loadState = newLoadState,
                exception = null,
                uiErrorMessage = null,
            )
        }
    }

    private fun sendEvent(newEvent: LoginUiEvent) = viewModelScope.launch {
        _uiEvent.emit(newEvent)
    }

    private suspend fun setLoginData(loginData: LoginData) {
        Timber.d("LoginData: loginUser=$loginData")

        /* CAUTION! **order of invocation is important to satisfy the reactive flow ** */
        viewModelState.update { state ->
            state.copy(
                isLoginSuccessful = true
            )
        }

        persistentStore.apply {
            setDeviceToken(loginData.deviceToken.nullAsEmpty())
            setRefreshToken(loginData.refreshToken.nullAsEmpty())
        }
        userDataRepository.setUserData(loginData.loginUser)
    }
}

interface LoginUiAction {
    data class ToggleConsentButton(val checked: Boolean) : LoginUiAction
    data class ErrorShown(val id: Long) : LoginUiAction
    data class TypingUsername(val typedUsername: String) : LoginUiAction
    data class TypingPassword(val typedPassword: String) : LoginUiAction
    data class Validate(val suppressError: Boolean, val isDeletedAccountRetrieve: Boolean) : LoginUiAction
    data object ResetLoading : LoginUiAction
}

interface LoginUiEvent {
    data class ShowToast(val message: UiText) : LoginUiEvent
    data class ShowSnack(val message: UiText) : LoginUiEvent
}

data class LoginViewModelState(
    val loadState: LoadStates = LoadStates.IDLE,
    val toggleButtonState: Boolean = false,
    val typedUsername: String = "",
    val typedPassword: String = "",

    /**
     * Flag to indicate that the login attempt is successful.
     */
    val isLoginSuccessful: Boolean = false,

    val exception: Throwable? = null,
    val uiErrorMessage: UiText? = null,
    val errors: List<ErrorMessage> = emptyList(),
)

private const val TOGGLE_BUTTON_STATE = "toggle_button_state"
private const val TYPED_USERNAME = "typed_username"
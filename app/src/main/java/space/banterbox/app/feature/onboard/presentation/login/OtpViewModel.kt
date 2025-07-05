package space.banterbox.app.feature.onboard.presentation.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.i18n.phonenumbers.PhoneNumberUtil
import space.banterbox.core.analytics.Analytics
import space.banterbox.core.analytics.AnalyticsLogger
import space.banterbox.app.Constant
import space.banterbox.app.R
import space.banterbox.app.common.util.InvalidOtpException
import space.banterbox.app.common.util.ResolvableException
import space.banterbox.app.common.util.UiText
import space.banterbox.app.core.util.Result
import space.banterbox.app.common.util.Util.countDownFlow
import space.banterbox.app.common.util.loadstate.LoadState
import space.banterbox.app.common.util.loadstate.LoadStates
import space.banterbox.app.common.util.loadstate.LoadType
import space.banterbox.app.core.di.AppDependencies
import space.banterbox.app.core.domain.model.CountryCodeModel
import space.banterbox.app.core.domain.repository.UserDataRepository
import space.banterbox.app.core.net.ApiException
import space.banterbox.app.core.net.NoInternetException
import space.banterbox.app.core.persistence.PersistentStore
import space.banterbox.app.feature.onboard.domain.model.LoginData
import space.banterbox.app.feature.onboard.domain.model.request.LoginRequest
import space.banterbox.app.feature.onboard.domain.repository.AccountsRepository
import space.banterbox.app.feature.onboard.presentation.util.RecaptchaException
import space.banterbox.app.ifDebug
import space.banterbox.app.nullAsEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    private val userDataRepository: UserDataRepository,
    private val persistentStore: PersistentStore,
    private val analyticsLogger: AnalyticsLogger,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<OtpUiState>(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<OtpUiEvent>()
    val uiEvent: SharedFlow<OtpUiEvent> = _uiEvent.asSharedFlow()

    val accept: (OtpUiAction) -> Unit

    private val actionsStream = MutableSharedFlow<OtpUiAction>()

    private var countDownTimerJob: Job? = null
    private var signupJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }

        actionsStream.filterIsInstance<OtpUiAction.TypingOtp>()
            .distinctUntilChanged()
            .onEach { action ->
                _uiState.update { it.copy(typedOtp = action.typedOtp) }
                if (action.typedOtp.length == 6) {
                    verifyOtpInternal()
                }
            }
            .launchIn(viewModelScope)

        startTimer(reset = true)
    }

    private fun onUiAction(action: OtpUiAction) {
        when (action) {
            is OtpUiAction.ErrorShown -> {
                _uiState.update { state ->
                    state.copy(
                        exception = null,
                        uiErrorMessage = null
                    )
                }
            }
            is OtpUiAction.TypingOtp -> {
                viewModelScope.launch { actionsStream.emit(action) }
            }
            is OtpUiAction.VerifyOtp -> {
                verifyOtpInternal()
            }
            is OtpUiAction.ResendOtp -> {
                resendOtp()
            }
            OtpUiAction.ResetLoading -> {
                setLoading(LoadType.ACTION, LoadState.NotLoading.InComplete)
            }
        }
    }

    fun handleBackPressed(): Boolean {
        val loadState = uiState.value.loadState
        return loadState.action is LoadState.Loading
    }

    fun setTypedOtp(otp: String) {
        _uiState.update { state -> state.copy(typedOtp = otp) }
    }

    fun getFormattedPhone(): String {
        val mobile = uiState.value.phoneNumber
        val countryCode = uiState.value.countryCode.replace(
            Regex("\\W"),
            ""
        )
        return try {
            val phoneNumberUtil = PhoneNumberUtil.getInstance()
            val phoneNumber = phoneNumberUtil.parse(
                mobile,
                CountryCodeModel.India.isocode
            )
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
            } else {
                throw IllegalArgumentException("Unable to parse mobile number")
            }
        } catch (e: Exception) {
            ifDebug { Timber.w(e, "$mobile") }
            "+${countryCode} $mobile"
        }
    }

    fun startTimer(reset: Boolean, growthRate: Float = 1F) {
        countDownTimerJob?.cancel(CancellationException("Timer hard reset received"))
        val countDownStart = (30 * growthRate).toInt()
        Timber.d("Count down to: $countDownStart rate = $growthRate")
        countDownTimerJob = countDownFlow(
            countDownStart.seconds,
            1.seconds
        )
            .onEach { tickMillis ->
                _uiState.update { state ->
                    state.copy(
                        tickMillis = tickMillis
                    )
                }
            }
            .onCompletion {
                /* Timer expired */
                _uiState.update { state ->
                    state.copy(
                        tickMillis = 0,
                        enableResendOtp = true
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * An API to expose the update of [OtpUiState.phoneNumber] and [OtpUiState.countryCode]
     */
    fun setPhoneNumber(countryCode: String, phoneNumber: String) {
        _uiState.update { state ->
            state.copy(
                phoneNumber = phoneNumber,
                countryCode = countryCode
            )
        }
    }

    fun setAccountType(type: String) {
        _uiState.update { state -> state.copy(accountType = type) }
    }

    fun resendOtp() {
        resendOtpInternal(recaptchaToken = "")
    }

    private fun validateOtp(): Boolean {
        /* Validate otp */
        val typedOtp = uiState.value.typedOtp.toString().trim()

        if (typedOtp.isBlank()) {
            val cause = IllegalStateException("No otp")
            _uiState.update { state ->
                state.copy(
                    exception = ResolvableException(cause),
                    uiErrorMessage = UiText.StringResource(R.string.please_enter_a_otp)
                )
            }
            return false
        } else if (typedOtp.length < 6) {
            val cause = IllegalStateException("Otp length error")
            _uiState.update { state ->
                state.copy(
                    exception = ResolvableException(cause),
                    uiErrorMessage = UiText.StringResource(R.string.otp_contains_at_least_6_digits)
                )
            }
            return false
        }
        return true
    }

    private fun verifyOtpInternal() {
        Timber.d("verifyOtpInternal() called")
        if (!validateOtp()) {
            return
        }

        Timber.d("verifyOtpInternal() called")
        val mobileNumber = uiState.value.phoneNumber
        val countryCode = uiState.value.countryCode
        val typedOtp = uiState.value.typedOtp
        val fcmToken = AppDependencies.persistentStore!!.fcmToken
        val type = uiState.value.accountType

        val request = LoginRequest(
           username = "",
            password = ""
        )

        AppDependencies.persistentStore?.apply {
            if (installReferrerFetched) {
                val key = deepLinkKey.first
                val value = deepLinkKey.second
            }
        }

        signInApi(request)
        /*if (uiState.value.isDeleteAccountRetrieve) {
            deleteAccountRetrieve(params)
        } else {
            signInApi(params)
        }*/
    }

    private fun resendOtpInternal(recaptchaToken: String) {
        val mobileNumber = uiState.value.phoneNumber
        val countryCode = uiState.value.countryCode
        val fcmToken = AppDependencies.persistentStore!!.fcmToken
        val type = uiState.value.accountType

        val request = LoginRequest(
            username = "",
            password = ""
        )

        signInApi(request)
        /*if (uiState.value.isDeleteAccountRetrieve) {
            deleteAccountRetrieve(params)
        } else {
            signInApi(params)
        }*/
    }

    private fun signInApi(request: LoginRequest) {
        if (signupJob?.isActive == true) {
            Timber.w("Signup request aborted. Already active.")
            return
        }
        signupJob?.cancel()
        setLoading(LoadType.ACTION, LoadState.Loading())
        signupJob = viewModelScope.launch {
            accountsRepository.loginUser(request).collectLatest { result ->
                when (result) {
                    is Result.Loading -> setLoading(LoadType.ACTION, LoadState.Loading())
                    is Result.Error -> {
                        when (result.exception) {
                            is ApiException -> {
                                when (result.exception.cause) {
                                    is InvalidOtpException -> {
                                        _uiState.update {
                                            it.copy(
                                                exception = ResolvableException(result.exception.cause),
                                                uiErrorMessage = UiText.StringResource(R.string.enter_a_valid_otp)
                                            )
                                        }
                                    }
                                    is RecaptchaException -> {
                                        _uiState.update {
                                            it.copy(
                                                exception = ResolvableException(result.exception.cause),
                                                uiErrorMessage = null
                                            )
                                        }
                                    }
                                    else -> {
                                        _uiState.update {
                                            it.copy(
                                                exception = result.exception,
                                                uiErrorMessage = UiText.somethingWentWrong
                                            )
                                        }
                                    }
                                }
                            }
                            is NoInternetException -> {
                                _uiState.update {
                                    it.copy(
                                        exception = result.exception,
                                        uiErrorMessage = UiText.noInternet
                                    )
                                }
                            }
                        }
                        setLoading(LoadType.ACTION, LoadState.Error(result.exception))
                        if (result.exception !is NoInternetException) {
                            analyticsLogger.logEvent(Analytics.Event.ONBOARD_OTP_FAIL_EVENT)
                        }
                    }
                    is Result.Success -> {
                        setLoading(LoadType.ACTION, LoadState.NotLoading.Complete)
                        if (false) {
                            val retryCount = uiState.value.retryCount.plus(1)
                            _uiState.update { state ->
                                state.copy(
                                    retryCount = retryCount,
                                    enableResendOtp = false,
                                )
                            }
                            Timber.d("Count down to: retry = $retryCount")
                            val growthRate = retryCount.plus(1).toFloat()
                            startTimer(reset = true, growthRate = growthRate)
                            sendEvent(OtpUiEvent.ShowToast(UiText.StringResource(R.string.otp_resent_successfully)))
                        } else {
                            /* CAUTION: ** order of invocation matters to satisfy the reactive conditions ** */
                            AppDependencies.persistentStore?.apply {
                                setFcmTokenSynced(true)
                                setLastTokenSyncTime(System.currentTimeMillis())
                                setInstallReferrerFetched(false)
                            }
                            userDataRepository.setShouldUpdateProfileOnce(result.data.showProfile)
                            setLoginData(result.data)
                            analyticsLogger.logEvent(Analytics.Event.ONBOARD_OTP_SUCCESS_EVENT)
                            sendEvent(OtpUiEvent.ShowToast(UiText.DynamicString("Login successful")))
                            /* Handled in MainViewModel */
                            // sendEvent(OtpUiEvent.NextScreen(result.data))
                        }
                    }
                }
            }
        }
    }

    private suspend fun setLoginData(loginData: LoginData) {
        Timber.d("LoginData: loginUser=$loginData")
        /* CAUTION! **order of invocation is important to satisfy the reactive flow ** */
        _uiState.update { state ->
            state.copy(
                isLoginSuccessful = true
            )
        }

        persistentStore.apply {
            setDeviceToken(loginData.deviceToken.nullAsEmpty())
        }
        userDataRepository.setUserData(loginData.loginUser)
    }

    private fun setLoading(
        loadType: LoadType,
        loadState: LoadState,
    ) {
        val newLoadState = uiState.value.loadState.modifyState(loadType, loadState)
        _uiState.update { state -> state.copy(loadState = newLoadState) }
    }

    private fun sendEvent(newEvent: OtpUiEvent) = viewModelScope.launch {
        _uiEvent.emit(newEvent)
    }

}

sealed interface OtpUiAction {
    data class TypingOtp(val typedOtp: String) : OtpUiAction
    data class VerifyOtp(val typedOtp: String) : OtpUiAction
    data class ErrorShown(val id: Long) : OtpUiAction
    data object ResetLoading : OtpUiAction
    data object ResendOtp : OtpUiAction
}

sealed interface OtpUiEvent {
    data class ShowToast(val message: UiText) : OtpUiEvent
}

data class OtpUiState(
    val loadState: LoadStates = LoadStates.IDLE,
    val phoneNumber: String = "",
    val countryCode: String = "",
    val typedOtp: String = "",
    val retryCount: Int = 0,
    val tickMillis: Long = 0,
    val enableResendOtp: Boolean = false,
    val accountType: String = "",

    /**
     * An otp to indicate that the login attempt is successful.
     */
    val isLoginSuccessful: Boolean = false,
    val exception: Exception? = null,
    val uiErrorMessage: UiText? = null
)
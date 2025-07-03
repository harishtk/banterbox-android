package space.banterbox.app.feature.onboard.presentation.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.i18n.phonenumbers.PhoneNumberUtil
import space.banterbox.app.Constant
import space.banterbox.app.R
import space.banterbox.app.common.util.ResolvableException
import space.banterbox.app.common.util.UiText
import space.banterbox.app.common.util.loadstate.LoadState
import space.banterbox.app.common.util.loadstate.LoadStates
import space.banterbox.app.common.util.loadstate.LoadType
import space.banterbox.app.core.Noop
import space.banterbox.app.core.data.repository.DefaultCountryCodeListRepository
import space.banterbox.app.core.di.AppDependencies
import space.banterbox.app.core.domain.model.CountryCodeModel
import space.banterbox.app.core.domain.repository.CountryCodeListRepository
import space.banterbox.app.core.net.ApiException
import space.banterbox.app.core.net.NoInternetException
import space.banterbox.app.core.util.ErrorMessage
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.onboard.domain.model.request.LoginRequest
import space.banterbox.app.feature.onboard.domain.repository.AccountsRepository
import space.banterbox.app.feature.onboard.presentation.util.InvalidMobileNumberException
import space.banterbox.app.feature.onboard.presentation.util.RecaptchaException
import space.banterbox.app.ifDebug
import space.banterbox.app.nullAsEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    defaultCountryCodeListRepository: CountryCodeListRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val viewModelState = MutableStateFlow<LoginViewModelState>(LoginViewModelState())
    val uiState = viewModelState
        .asStateFlow()

    private val _uiEvent = MutableSharedFlow<LoginUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (LoginUiAction) -> Unit

    private val typingState = MutableSharedFlow<LoginUiAction.TypingPhone>()

    // Api values
    private var recaptchaToken: String = ""

    private var signupJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }

        val toggleButtonState: Boolean = savedStateHandle[TOGGLE_BUTTON_STATE] ?: false
        viewModelState.update { it.copy(toggleButtonState = toggleButtonState) }

        val typedPhone = savedStateHandle[TYPED_PHONE] ?: ""
        viewModelState.update { it.copy(typedPhone = typedPhone) }

        typingState
            .distinctUntilChanged()
            .onEach { action ->
                viewModelState.update { it.copy(typedPhone = action.typedPhone) }
            }
            .launchIn(viewModelScope)

        defaultCountryCodeListRepository.countryCodeModelListStream
            .onEach { countryCodeModels ->
                setCountryCodeList(countryCodeModels)
                setCountryCode(CountryCodeModel.India)
            }
            .launchIn(viewModelScope)

        ifDebug { setPhoneNumber("6505551235") }
    }

    private fun onUiAction(action: LoginUiAction) {
        when (action) {
            is LoginUiAction.ErrorShown -> {
                popError(action.id)
            }
            is LoginUiAction.ToggleConsentButton -> {
                viewModelState.update { it.copy(toggleButtonState = action.checked) }
            }
            is LoginUiAction.SetCountryCode -> {
                setCountryCodeModelInternal(action.countryCodeModel)
            }
            is LoginUiAction.TypingPhone -> {
                viewModelScope.launch { typingState.emit(action) }
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

    fun getFormattedPhone(): String {
        val mobile = uiState.value.typedPhone
        val countryCode = uiState.value.countryCodeModel?.dialcode?.replace(
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
            ifDebug { Timber.w(e) }
            "+${countryCode} $mobile"
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

        // Phone validation
        val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
        try {
            val phoneNumber = viewModelState.value.typedPhone
            val isoCode = viewModelState.value.countryCodeModel?.isocode
            val phoneNumberLength = phoneNumberUtil.parse(phoneNumber, isoCode)
            if (!phoneNumberUtil.isValidNumber(phoneNumberLength)) {
                val cause = InvalidMobileNumberException("Enter a valid phone")
                addError(
                    t = ResolvableException(cause),
                    uiText = UiText.StringResource(R.string.enter_a_valid_phone)
                )
                return
            }
        } catch (e: Exception) {
            Timber.tag("ERROR").e(e)
            val cause = InvalidMobileNumberException("Enter a valid phone")
            addError(
                t = ResolvableException(cause),
                uiText = UiText.StringResource(R.string.enter_a_valid_phone)
            )
            return
        }

        // toggle button validation
        /*if (!viewModelState.value.toggleButtonState) {
            val cause = IllegalStateException("Consent button is not checked")
            addError(
                t = ResolvableException(cause),
                uiText = UiText.StringResource(R.string.read_and_accept_terms)
            )
            return@launch
        }*/

        signInApiInternal()
        /*if (isDeletedAccountRetrieve) {
            retrieveDeletedAccountInternal()
        } else {
            signInApiInternal()
        }*/
    }

    fun requestAccountRecoveryOtp(type: String) {
        requestAccountRecoveryOtpInternal(type)
    }

    private fun requestAccountRecoveryOtpInternal(type: String) {
        viewModelState.update { state -> state.copy(accountType = type) }
        val mobileNumber = viewModelState.value.typedPhone
        val countryCode = viewModelState.value.countryCodeModel?.dialcode.nullAsEmpty()

        val request = LoginRequest(
            phoneNumber = mobileNumber,
            countryCode = countryCode.replace("+", ""),
            guestUserId = 0,
            callFor = CALL_FOR_SEND_OTP,
            platform = Constant.PLATFORM,
            fcm = AppDependencies.persistentStore?.fcmToken.nullAsEmpty(),
            type = type
        )

        login(request)
    }

    private fun signInApiInternal(recaptchaToken: String = "") {
        val mobileNumber = viewModelState.value.typedPhone
        val countryCode = viewModelState.value.countryCodeModel?.dialcode.nullAsEmpty()

        val request = LoginRequest(
            phoneNumber = mobileNumber,
            countryCode = countryCode.replace("+", ""),
            guestUserId = 0,
            callFor = CALL_FOR_SEND_OTP,
            platform = Constant.PLATFORM,
            fcm = AppDependencies.persistentStore?.fcmToken.nullAsEmpty(),
            type = ""
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
            accountsRepository.loginUser(request).collectLatest { result ->
                when (result) {
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
                        viewModelState.update { state ->
                            state.copy(
                                isOtpSent = true
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * This function exposed the updating of [LoginViewModel.recaptchaToken]
     */
    fun setRecaptchaToken(recaptchaToken: String) {
        this.recaptchaToken = recaptchaToken

        // TODO: (deferred) login - sign in with the recaptcha token.
        // signInApiInternal(recaptchaToken)
    }

    /**
     * This function exposes the updating of [LoginViewModelState.countryCodeList]
     */
    private fun setCountryCodeList(newCountryCodeList: List<CountryCodeModel>) {
        viewModelState.update { it.copy(countryCodeList = newCountryCodeList) }
    }

    /**
     * This function exposes the updating of [LoginViewModelState.countryCodeModel]
     */
    private fun setCountryCode(newCountryCode: CountryCodeModel) {
        setCountryCodeModelInternal(newCountryCode)
        Timber.d("setCountryCode() called with: newCountryCode = [$newCountryCode]")
    }

    /**
     * This function exposes the updating of [LoginViewModelState.typedPhone]
     */
    fun setPhoneNumber(phone: String) {
        viewModelState.update { it.copy(typedPhone = phone) }
    }

    fun resetOtpSent() {
        viewModelState.update { it.copy(isOtpSent = false) }
    }

    fun handleBackPressed(): Boolean {
        return false
    }

    private fun setCountryCodeModelInternal(countryCodeModel: CountryCodeModel) {
        viewModelState.update { it.copy(countryCodeModel = countryCodeModel) }
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

}
interface LoginUiAction {
    data class ToggleConsentButton(val checked: Boolean) : LoginUiAction
    data class ErrorShown(val id: Long) : LoginUiAction
    data class SetCountryCode(val countryCodeModel: CountryCodeModel) : LoginUiAction
    data class TypingPhone(val typedPhone: String) : LoginUiAction
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
    val typedPhone: String = "",
    val countryCodeModel: CountryCodeModel = CountryCodeModel.India,
    val countryCodeList: List<CountryCodeModel> = emptyList(),
    /**
     * A type to support deleted account recovery.
     */
    val accountType: String = "",

    /**
     * Flag to indicate that the send otp attempt is successful.
     */
    val isOtpSent: Boolean = false,
    val exception: Throwable? = null,
    val uiErrorMessage: UiText? = null,
    val errors: List<ErrorMessage> = emptyList(),
)

internal const val CALL_FOR_SEND_OTP = "sendOtp"
internal const val CALL_FOR_VERIFY_OTP = "verifyOtp"

private const val TOGGLE_BUTTON_STATE = "toggle_button_state"
private const val TYPED_PHONE = "typed_phone"
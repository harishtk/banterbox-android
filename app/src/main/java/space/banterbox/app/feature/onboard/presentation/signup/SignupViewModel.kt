package space.banterbox.app.feature.onboard.presentation.signup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.banterbox.app.common.util.UiText
import space.banterbox.app.common.util.loadstate.LoadState
import space.banterbox.app.common.util.loadstate.LoadStates
import space.banterbox.app.common.util.loadstate.LoadType
import space.banterbox.app.core.net.ApiException
import space.banterbox.app.core.net.NoInternetException
import space.banterbox.app.core.util.ErrorMessage
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.onboard.domain.model.request.SignupRequest
import space.banterbox.app.feature.onboard.domain.repository.AuthRepository
import space.banterbox.app.feature.onboard.presentation.util.UsernameUnavailableException
import space.banterbox.app.ifDebug
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    
    private val viewModelState = MutableStateFlow(ViewModelState())

    val signupUiState = viewModelState
        .map(ViewModelState::toSignupUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = viewModelState.value.toSignupUiState()
        )

    val accept: (SignupUiAction) -> Unit

    private var signupJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }
    }

    private fun onUiAction(action: SignupUiAction) {
        when (action) {
            SignupUiAction.ErrorShown -> {

            }
            SignupUiAction.Reset -> {
                viewModelState.update { state ->
                    state.copy(
                        loadState = LoadStates.IDLE,
                        isSignupSuccessful = false,
                        username = "",
                        password = "",
                        displayName = "",
                        bio = "",
                    )
                }
            }
            is SignupUiAction.Submit -> {
                viewModelState.update { state ->
                    state.copy(
                        username = action.username,
                        password = action.password,
                        displayName = action.displayName,
                        bio = action.bio
                    )
                }
                validate()
            }
        }
    }

    private fun validate() {
        // No validation required.

        val request = SignupRequest(
            username = viewModelState.value.username,
            password = viewModelState.value.password,
            displayName = viewModelState.value.displayName,
            bio = viewModelState.value.bio
        )
        signup(request);
    }

    private fun signup(request: SignupRequest) {
        if (signupJob?.isActive == true) {
            val t = IllegalStateException("A request is already active.")
            ifDebug { Timber.w(t) }
            return
        }

        signupJob?.cancel(CancellationException())
        setLoading(LoadType.ACTION, LoadState.Loading())
        signupJob = viewModelScope.launch {
            when (val result = authRepository.signup(request)) {
                Result.Loading -> {}
                is Result.Error -> {
                    val errorMessage = when (result.exception) {
                        is ApiException -> {
                            when (result.exception.cause) {
                                is UsernameUnavailableException -> {
                                    ErrorMessage(
                                        id = 2,
                                        exception = result.exception,
                                        message = UiText.DynamicString("Username is taken!")
                                    )
                                }

                                else -> {
                                    ErrorMessage(
                                        id = 3,
                                        exception = result.exception,
                                        message = UiText.somethingWentWrong
                                    )
                                }
                            }
                        }
                        is NoInternetException -> {
                            ErrorMessage(
                                id = 1,
                                exception = result.exception,
                                message = UiText.noInternet
                            )
                        }

                        else -> {
                            ErrorMessage(
                                id = 0,
                                exception = result.exception,
                                message = UiText.somethingWentWrong
                            )
                        }
                    }
                    viewModelState.update { state ->
                        state.copy(
                            errorMessage = errorMessage
                        )
                    }
                    setLoading(LoadType.ACTION, LoadState.Error(result.exception))
                }

                is Result.Success -> {
                    viewModelState.update { state ->
                        state.copy(
                            isSignupSuccessful = true
                        )
                    }
                    setLoading(LoadType.ACTION, LoadState.NotLoading.Complete)
                }
            }
        }

    }

    private fun setLoading(
        loadType: LoadType,
        loadState: LoadState,
    ) {
        val newLoadState = viewModelState.value.loadState.modifyState(loadType, loadState)
        viewModelState.update { state -> state.copy(loadState = newLoadState) }
    }
}

private data class ViewModelState(
    val loadState: LoadStates = LoadStates.IDLE,

    val username: String = "",
    val password: String = "",
    val displayName: String = "",
    val bio: String = "",
    val errorMessage: ErrorMessage? = null,

    /**
     * Flag to indicate that the signup is successful
     */
    val isSignupSuccessful: Boolean = false,
) {
    fun toSignupUiState(): SignupUiState {
        return if (isSignupSuccessful) {
            SignupUiState.SignupSuccess
        } else {
            SignupUiState.SignupForm(
                username = username,
                password = password,
                displayName = displayName,
                bio = bio,
                errorMessage = errorMessage,
            )
        }
    }
}

sealed interface SignupUiState {
    data class SignupForm(
        val username: String,
        val password: String,
        val displayName: String,
        val bio: String,
        val errorMessage: ErrorMessage? = null,
    ) : SignupUiState

    data object SignupSuccess : SignupUiState
}

sealed interface SignupUiAction {
    data object ErrorShown : SignupUiAction
    data class Submit(
        val username: String,
        val password: String,
        val displayName: String,
        val bio: String,
    ) : SignupUiAction
    data object Reset : SignupUiAction
}
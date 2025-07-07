package space.banterbox.app.feature.home.presentation.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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
import space.banterbox.app.feature.home.domain.model.UserProfile
import space.banterbox.app.feature.home.domain.repository.UserRepository
import space.banterbox.app.feature.home.domain.util.UserNotFoundException
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val viewModelState = MutableStateFlow(ViewModelState())

    val profileUiState = viewModelState
        .map(ViewModelState::toProfileUiState)
        .onStart { getUserProfileInternal() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState.Idle
        )

    private val _uiEvent = MutableSharedFlow<ProfileUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (ProfileUiAction) -> Unit

    private var profileFetchJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction)}
    }

    private fun onUiAction(uiAction: ProfileUiAction) {
        when (uiAction) {
            ProfileUiAction.Refresh -> getUserProfileInternal()
        }
    }

    private fun getUserProfileInternal() {
        if (profileFetchJob?.isActive == true) {
            val t = IllegalStateException("Profile fetch job is already active, cancelling")
            Timber.w(t)
        }

        profileFetchJob?.cancel(CancellationException("New request"))
        setLoading(LoadType.REFRESH, LoadState.Loading())
        profileFetchJob = viewModelScope.launch {
            when (val result = userRepository.getOwnUser()) {
                Result.Loading -> {}
                is Result.Error -> {
                    val errorMessage = when (result.exception) {
                        is ApiException -> {
                            when (result.exception.cause) {
                                is UserNotFoundException -> {
                                    ErrorMessage(
                                        id = 2,
                                        exception = result.exception,
                                        message = UiText.DynamicString("User not found")
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
                    setLoading(LoadType.REFRESH, LoadState.Error(result.exception))
                }
                is Result.Success -> {
                    viewModelState.update { state ->
                        state.copy(
                            profile = result.data
                        )
                    }
                    setLoading(LoadType.REFRESH, LoadState.NotLoading.Complete)
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

    private fun sendEvent(newEvent: ProfileUiEvent) = viewModelScope.launch {
        _uiEvent.emit(newEvent)
    }

}

private data class ViewModelState(
    val loadState: LoadStates = LoadStates.IDLE,
    val profile: UserProfile? = null,
    val errorMessage: ErrorMessage? = null,
) {
    fun toProfileUiState(): ProfileUiState {
        if (profile != null) {
            return ProfileUiState.Success(profile)
        } else {
            when (loadState.refresh) {
                is LoadState.Loading -> {
                    return ProfileUiState.Loading
                }

                is LoadState.Error -> {
                    val message = errorMessage ?: ErrorMessage.unknown()
                    return ProfileUiState.Error(message)
                }

                else -> {
                    return ProfileUiState.Idle
                }
            }
        }
    }
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data object Idle : ProfileUiState
    data class Error(val errorMessage: ErrorMessage) : ProfileUiState
    data class Success(val profile: UserProfile) : ProfileUiState
}

sealed interface ProfileUiAction {
    data object Refresh : ProfileUiAction
}

sealed interface ProfileUiEvent {
    data class ShowToast(val message: UiText) : ProfileUiEvent
    data class ShowSnackbar(val message: UiText) : ProfileUiEvent
}

const val USER_ID = "userId"
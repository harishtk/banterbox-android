package space.banterbox.app.feature.onboard.presentation.boarding

import androidx.compose.animation.core.updateTransition
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import space.banterbox.app.common.util.UiText
import space.banterbox.app.common.util.loadstate.LoadState
import space.banterbox.app.common.util.loadstate.LoadStates
import space.banterbox.app.common.util.loadstate.LoadType
import space.banterbox.app.core.domain.repository.ShopDataRepository
import space.banterbox.app.core.domain.repository.UserDataRepository
import space.banterbox.app.core.net.ApiException
import space.banterbox.app.core.net.NoInternetException
import space.banterbox.app.core.util.ErrorMessage
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.onboard.domain.repository.AccountsRepository
import space.banterbox.app.ifDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LaunchStoreViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    private val userDataRepository: UserDataRepository,
    private val shopDataRepository: ShopDataRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LaunchStoreUiState>(LaunchStoreUiState())
    val uiState: StateFlow<LaunchStoreUiState> = _uiState.asStateFlow()

    private var launchStoreJob: Job? = null

    init {
        shopDataRepository.shopData
            .map { it.name }
            .onEach { storeName ->
                _uiState.update { state ->
                    state.copy(
                        storeName = storeName
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun launchStore() {
        _uiState.update { state ->
            state.copy(
                errorMessage = null
            )
        }
        launchStoreInternal()
    }

    private fun launchStoreInternal() {
        if (launchStoreJob?.isActive == true) {
            val t = IllegalStateException("A request is already active. Ignoring.")
            ifDebug { Timber.w(t) }
            return
        }

        launchStoreJob?.cancel(CancellationException("New request"))
        setLoading(LoadType.ACTION, LoadState.Loading())
        launchStoreJob = viewModelScope.launch {
            when (val result = accountsRepository.launchStore()) {
                Result.Loading -> {}
                is Result.Error -> {
                    val errorMessage = when (result.exception) {
                        is ApiException -> {
                            ErrorMessage(
                                id = 2,
                                exception = result.exception,
                                message = UiText.somethingWentWrong
                            )
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
                    _uiState.update { state ->
                        state.copy(
                            errorMessage = errorMessage
                        )
                    }
                    setLoading(LoadType.ACTION, LoadState.Error(result.exception))
                }

                is Result.Success -> {
                    userDataRepository.updateOnboardStep(result.data)
                    _uiState.update { state ->
                        state.copy(
                            isLaunched = true
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
        val newLoadState = uiState.value.loadState.modifyState(loadType, loadState)
        _uiState.update { state -> state.copy(loadState = newLoadState) }
    }
}

data class LaunchStoreUiState(
    val loadState: LoadStates = LoadStates.IDLE,
    val storeName: String = "",
    val isLaunched: Boolean = false,
    val errorMessage: ErrorMessage? = null
)
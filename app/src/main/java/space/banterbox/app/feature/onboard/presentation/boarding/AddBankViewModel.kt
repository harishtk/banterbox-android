package space.banterbox.app.feature.onboard.presentation.boarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import space.banterbox.app.common.util.UiText
import space.banterbox.app.common.util.loadstate.LoadState
import space.banterbox.app.common.util.loadstate.LoadStates
import space.banterbox.app.common.util.loadstate.LoadType
import space.banterbox.app.core.domain.repository.UserDataRepository
import space.banterbox.app.core.net.ApiException
import space.banterbox.app.core.net.NoInternetException
import space.banterbox.app.core.util.ErrorMessage
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.onboard.domain.model.request.AddBankRequest
import space.banterbox.app.feature.onboard.domain.repository.AccountsRepository
import space.banterbox.app.ifDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddBankViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    private val userDataRepository: UserDataRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val viewModelState = MutableStateFlow(ViewModelState())

    val addBankUiState = viewModelState
        .map(ViewModelState::toAddBankUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = viewModelState.value.toAddBankUiState()
        )

    val accept: (AddBankUiAction) -> Unit

    private var addBankJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }
    }

    private fun onUiAction(action: AddBankUiAction) {
        when (action) {
            AddBankUiAction.ErrorShown -> {

            }
            AddBankUiAction.Reset -> {
                viewModelState.update { state ->
                    state.copy(
                        loadState = LoadStates.IDLE,
                        isBankAdded = false,
                        accountNumber = "",
                        accountHolderName = "",
                        ifscNumber = "",
                    )
                }
            }
            is AddBankUiAction.Submit -> {
                viewModelState.update { state ->
                    state.copy(
                        accountNumber = action.accountNumber,
                        accountHolderName = action.accountHolderName,
                        ifscNumber = action.ifscNumber
                    )
                }
                validate()
            }
        }
    }

    private fun validate() {
        // No validation required.

        val request = AddBankRequest(
            accountNumber = viewModelState.value.accountNumber,
            accountHolderName = viewModelState.value.accountHolderName,
            ifscCode = viewModelState.value.ifscNumber
        )

        addBankAccount(request)
    }

    private fun addBankAccount(request: AddBankRequest) {
        if (addBankJob?.isActive == true) {
            val t = IllegalStateException("Already a request is active.")
            ifDebug { Timber.e(t) }
            return
        }

        addBankJob?.cancel(CancellationException())
        setLoading(LoadType.ACTION, LoadState.Loading())
        addBankJob = viewModelScope.launch {
            when (val result = accountsRepository.addBank(request)) {
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
                    viewModelState.update { state ->
                        state.copy(
                            errorMessage = errorMessage
                        )
                    }
                    setLoading(LoadType.ACTION, LoadState.Error(result.exception))
                }
                is Result.Success -> {
                    userDataRepository.updateOnboardStep(result.data)
                    viewModelState.update { state ->
                        state.copy(
                            isBankAdded = true
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

    val accountNumber: String = "",
    val accountHolderName: String = "",
    val ifscNumber: String = "",
    val errorMessage: ErrorMessage? = null,

    /**
     * Flag to indicate that the product is added. Used for navigation.
     */
    val isBankAdded: Boolean = false,
) {
    fun toAddBankUiState(): AddBankUiState {
        return if (isBankAdded) {
            AddBankUiState.BankAdded
        } else if (loadState.action is LoadState.Loading) {
            AddBankUiState.TestingPurchase
        } else {
            AddBankUiState.BankDetailForm(
                accountNumber = accountNumber,
                accountHolderName = accountHolderName,
                ifscNumber = ifscNumber,
                errorMessage = errorMessage,
            )
        }
    }
}

interface AddBankUiState {
    data class BankDetailForm(
        val accountNumber: String,
        val accountHolderName: String,
        val ifscNumber: String,
        val errorMessage: ErrorMessage? = null,
    ) : AddBankUiState

    data object TestingPurchase : AddBankUiState

    data object BankAdded : AddBankUiState
}

interface AddBankUiAction {
    data object ErrorShown : AddBankUiAction
    data class Submit(
        val accountNumber: String,
        val accountHolderName: String,
        val ifscNumber: String,
    ) : AddBankUiAction
    data object Reset : AddBankUiAction
}
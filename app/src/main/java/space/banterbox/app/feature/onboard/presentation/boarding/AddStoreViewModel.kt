package space.banterbox.app.feature.onboard.presentation.boarding

import androidx.compose.material3.TimeInput
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import space.banterbox.app.common.util.UiText
import space.banterbox.app.common.util.loadstate.LoadState
import space.banterbox.app.common.util.loadstate.LoadStates
import space.banterbox.app.common.util.loadstate.LoadType
import space.banterbox.app.core.di.AppDependencies
import space.banterbox.app.core.domain.repository.UserDataRepository
import space.banterbox.app.core.net.ApiException
import space.banterbox.app.core.net.NoInternetException
import space.banterbox.app.core.util.Result
import space.banterbox.app.core.util.UnknownException
import space.banterbox.app.feature.onboard.domain.model.ShopCategory
import space.banterbox.app.feature.onboard.domain.model.request.AddStoreRequest
import space.banterbox.app.feature.onboard.domain.model.request.StoreCategoryRequest
import space.banterbox.app.feature.onboard.domain.repository.AccountsRepository
import space.banterbox.app.ifDebug
import space.banterbox.app.nullAsEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddStoreViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    private val userDataRepository: UserDataRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val viewModelState = MutableStateFlow(AddStoreViewModelState())

    val addStoreUiState: StateFlow<AddStoreUiState> = viewModelState
        .map(AddStoreViewModelState::toAddStoreUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = viewModelState.value.toAddStoreUiState()
        )

    val storeCategoriesUiState: StateFlow<StoreCategoriesUiState> = viewModelState
        .map(AddStoreViewModelState::toStoreCategoriesUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = viewModelState.value.toStoreCategoriesUiState()
        )

    private val _uiEvent = MutableSharedFlow<AddStoreUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (AddStoreUiAction) -> Unit

    private val actionStream = MutableSharedFlow<AddStoreUiAction>()

    private var refreshCategoriesJob: Job? = null
    private var addStoreJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }

        actionStream
            .filterIsInstance<AddStoreUiAction.TypingStoreName>()
            .distinctUntilChanged()
            .onEach { action ->
                Timber.d("storeName: ${action.value}")
                viewModelState.update { state ->
                    state.copy(
                        storeName = action.value
                    )
                }
            }
            .launchIn(viewModelScope)

        accountsRepository.storeCategories("")
            .distinctUntilChanged()
            .map { shopCategories ->
                viewModelState.update { state ->
                    state.copy(
                        storeCategories = shopCategories
                    )
                }
            }
            .launchIn(viewModelScope)
        refreshCategoriesInternal(true)
    }

    private fun onUiAction(action: AddStoreUiAction) {
        when (action) {
            is AddStoreUiAction.TypingStoreName -> {
                viewModelScope.launch { actionStream.emit(action) }
            }
            is AddStoreUiAction.ErrorShown -> {
                // TODO: reset errors?
            }

            is AddStoreUiAction.ToggleSelectedCategory -> {
                viewModelState.update { state ->
                    state.copy(
                        selectedStoreCategory = action.category
                    )
                }
            }

            is AddStoreUiAction.Submit -> {
                // TODO: validate
                validateInternal(action.storeName, action.storeCategory)
            }

            AddStoreUiAction.Reset -> {
                viewModelState.update { state ->
                    state.copy(
                        loadState = LoadStates.IDLE,
                        storeName = "",
                        selectedStoreCategory = null,
                        storeCategoryLoadState = LoadStates.IDLE,
                        isStoreAddedSuccessfully = false,
                    )
                }
            }

            AddStoreUiAction.ResetLoading -> {
                setLoadState("store_categories", LoadType.REFRESH, LoadState.NotLoading.InComplete)
                setLoadState("default", LoadType.REFRESH, LoadState.NotLoading.InComplete)
            }

            AddStoreUiAction.Refresh -> {
                refreshCategoriesInternal()
            }
        }
    }

    private fun validateInternal(storeName: String, storeCategory: String) {
        // TODO: add store

        /*viewModelScope.launch {
            setLoadState(loadType = LoadType.ACTION, loadState = LoadState.Loading())
            delay(500)
            setLoadState(loadType = LoadType.ACTION, loadState = LoadState.NotLoading.Complete)
            viewModelState.update { state ->
                state.copy(
                    isStoreAddedSuccessfully = true
                )
            }
        }*/
        val request = AddStoreRequest(
            tempId = AppDependencies.persistentStore?.tempId.nullAsEmpty(),
            storeName = viewModelState.value.storeName,
            storeCategory = viewModelState.value.selectedStoreCategory?.categoryName.nullAsEmpty(),
            fcm = AppDependencies.persistentStore?.fcmToken.nullAsEmpty(),
        )

        addStore(request)
    }

    private fun addStore(addStoreRequest: AddStoreRequest) {
        if (addStoreJob?.isActive == true) {
            Timber.w("Add Store request aborted. Already active.")
            return
        }

        addStoreJob?.cancel()
        setLoadState(loadType = LoadType.ACTION, loadState = LoadState.Loading())
        addStoreJob = viewModelScope.launch {
            when (val result = accountsRepository.addStore(addStoreRequest)) {
                Result.Loading -> {}
                is Result.Error -> {
                    setLoadState(loadType = LoadType.ACTION, loadState = LoadState.Error(result.exception))
                }
                is Result.Success -> {
                    setLoadState(loadType = LoadType.ACTION, loadState = LoadState.NotLoading.Complete)
                    userDataRepository.setUserData(result.data.loginUser)
                    viewModelState.update { state ->
                        state.copy(
                            isStoreAddedSuccessfully = true
                        )
                    }
                }
            }
        }
    }

    private fun refreshCategoriesInternal(suppressErrors: Boolean = false) {
        if (refreshCategoriesJob?.isActive == true) {
            val t = IllegalStateException("Already a refresh job is active")
            ifDebug { Timber.w(t) }
            return
        }

        refreshCategoriesJob?.cancel(CancellationException("New request")) // just in case

        val request = StoreCategoryRequest("")

        setLoadState("store_categories", LoadType.REFRESH, LoadState.Loading())
        refreshCategoriesJob = viewModelScope.launch {
            val result = accountsRepository.refreshCategories(request)
            Timber.d("Refresh: result=$result")
            when (result) {
                Result.Loading -> {}
                is Result.Error -> {
                    when (result.exception) {
                        is ApiException -> {
                            setLoadState(
                                "store_categories",
                                LoadType.REFRESH,
                                LoadState.Error(result.exception)
                            )
                        }

                        is NoInternetException -> {
                            setLoadState(
                                "store_categories",
                                LoadType.REFRESH,
                                LoadState.Error(result.exception)
                            )
                        }

                        else -> {
                            setLoadState(
                                "store_categories",
                                LoadType.REFRESH,
                                LoadState.Error(UnknownException(result.exception))
                            )
                        }
                    }
                }

                is Result.Success -> {
                    setLoadState("store_categories", LoadType.REFRESH, LoadState.NotLoading.Complete)
                }
            }
        }
    }

    private fun setLoadState(
        from: String = "default",
        loadType: LoadType,
        loadState: LoadState,
    ) {
        if (from == "store_categories") {
            val newLoadState = viewModelState.value.storeCategoryLoadState.modifyState(loadType, loadState)
            viewModelState.update { state ->
                state.copy(
                    storeCategoryLoadState = newLoadState
                )
            }
        } else {
            val newLoadState = viewModelState.value.storeCategoryLoadState.modifyState(loadType, loadState)
            viewModelState.update { state ->
                state.copy(
                    loadState = newLoadState
                )
            }
        }
    }

    private fun sendEvent(newEvent: AddStoreUiEvent) = viewModelScope.launch {
        _uiEvent.emit(newEvent)
    }

}

/**
 * TODO: Can we derive this from a new 'ViewModelState' class?
 *          and use the same for [StoreCategoriesUiState]?
 */
data class AddStoreUiState(
    val loadState: LoadStates = LoadStates.IDLE,
    val storeName: String = "",
    val storeCategory: ShopCategory? = null,
    val storeAdded: Boolean = false,
)

interface AddStoreUiAction {
    data class ErrorShown(val id: Long) : AddStoreUiAction
    data class TypingStoreName(val value: String) : AddStoreUiAction
    data class ToggleSelectedCategory(val category: ShopCategory) : AddStoreUiAction
    data class Submit(val storeName: String, val storeCategory: String) : AddStoreUiAction
    data object Reset : AddStoreUiAction
    data object ResetLoading : AddStoreUiAction
    data object Refresh: AddStoreUiAction
}

interface AddStoreUiEvent {
    data class ShowToast(val message: UiText) : AddStoreUiEvent
    data class ShowSnack(val message: UiText) : AddStoreUiEvent
}

interface StoreCategoriesUiState {
    data class CategoriesList(
        val shopCategories: List<ShopCategory>,
        val selectedCategory: ShopCategory?,
        val isLoading: Boolean,
    ) : StoreCategoriesUiState
    data class Error(val exception: Exception) : StoreCategoriesUiState
    data object Loading : StoreCategoriesUiState

    data object Idle : StoreCategoriesUiState
}

private data class AddStoreViewModelState(
    val storeCategoryLoadState: LoadStates = LoadStates.IDLE,
    val storeCategories: List<ShopCategory> = emptyList(),
    val selectedStoreCategory: ShopCategory? = null,

    val loadState: LoadStates = LoadStates.IDLE,
    val storeName: String = "",

    /**
     * Flag to indicate that the store is added. Used for navigation.
     */
    val isStoreAddedSuccessfully: Boolean = false,
) {
    fun toStoreCategoriesUiState(): StoreCategoriesUiState {
        return if (storeCategories.isEmpty()) {
            when (val loadState = storeCategoryLoadState.refresh) {
                is LoadState.Loading -> {
                    StoreCategoriesUiState.Loading
                }
                is LoadState.Error -> {
                    StoreCategoriesUiState.Error(
                        Exception(loadState.error)
                    )
                }

                else -> StoreCategoriesUiState.Idle
            }
        } else {
            StoreCategoriesUiState.CategoriesList(
                shopCategories = storeCategories,
                selectedCategory = selectedStoreCategory,
                isLoading = storeCategoryLoadState.refresh is LoadState.Loading
            )
        }
    }

    fun toAddStoreUiState(): AddStoreUiState {
        return AddStoreUiState(
            loadState = loadState,
            storeName = storeName,
            storeCategory = selectedStoreCategory,
            storeAdded = isStoreAddedSuccessfully,
        )
    }
}
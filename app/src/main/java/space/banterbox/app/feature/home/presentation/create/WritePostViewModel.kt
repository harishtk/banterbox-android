package space.banterbox.app.feature.home.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
import space.banterbox.app.feature.home.domain.model.request.CreatePostRequest
import space.banterbox.app.feature.home.domain.repository.PostRepository
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class WritePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val viewModelState = MutableStateFlow(ViewModelState())

    val postUiState = viewModelState
        .map(ViewModelState::toPostUiState)
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
            initialValue = viewModelState.value.toPostUiState()
        )

    private val _uiEvent = MutableSharedFlow<WritePostUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val actionStream = MutableSharedFlow<WritePostUiAction>()

    val accept: (WritePostUiAction) -> Unit

    private var postSubmitJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }

        viewModelState.update { state -> state.copy(content = savedStateHandle[CONTENT] ?: "") }

        actionStream.filterIsInstance<WritePostUiAction.OnContentChange>()
            .distinctUntilChanged()
            .onEach { action ->
                viewModelState.update { state ->
                    state.copy(content = action.content)
                }
                savedStateHandle[CONTENT] = action.content
            }
            .launchIn(viewModelScope)
    }

    private fun onUiAction(uiAction: WritePostUiAction) {
        when (uiAction) {
            is WritePostUiAction.OnContentChange -> {
                viewModelScope.launch {
                    actionStream.emit(uiAction)
                }
            }
            WritePostUiAction.Submit -> {
                submitPostInternal()
            }
            WritePostUiAction.Reset -> {
                savedStateHandle[CONTENT] = ""
                viewModelState.update { state ->
                    state.copy(
                        content = "",
                        errorMessage = null,
                        loadState = LoadStates.IDLE,
                        isPostedSuccessfully = false
                    )
                }
            }
        }
    }

    private fun submitPostInternal() {
        if (postSubmitJob?.isActive == true) {
            val t = IllegalStateException("Post submit job is already active, cancelling")
            Timber.w(t)
        }

        postSubmitJob?.cancel(CancellationException("New request"))
        setLoading(LoadType.ACTION, LoadState.Loading())

        val request = CreatePostRequest(
            content = viewModelState.value.content
        )

        postSubmitJob = viewModelScope.launch {
            when (val result = postRepository.createPost(request)) {
                Result.Loading -> {}
                is Result.Error -> {
                    when (result.exception) {
                        is ApiException -> {
                            val errorMessage = ErrorMessage(
                                id = 0,
                                exception = result.exception,
                                message = UiText.somethingWentWrong
                            )
                            viewModelState.update { state ->
                                state.copy(
                                    errorMessage = errorMessage
                                )
                            }
                        }

                        is NoInternetException -> {
                            val errorMessage = ErrorMessage(
                                id = 1,
                                exception = result.exception,
                                message = UiText.noInternet
                            )
                            viewModelState.update { state ->
                                state.copy(
                                    errorMessage = errorMessage
                                )
                            }
                        }

                        else -> {
                            val errorMessage = ErrorMessage(
                                id = 0,
                                exception = result.exception,
                                message = UiText.somethingWentWrong
                            )
                            viewModelState.update { state ->
                                state.copy(
                                    errorMessage = errorMessage
                                )
                            }
                        }
                    }
                    setLoading(LoadType.ACTION, LoadState.Error(result.exception))
                }
                is Result.Success -> {
                    setLoading(LoadType.ACTION, LoadState.NotLoading.Complete)
                    viewModelState.update { state ->
                        state.copy(
                            isPostedSuccessfully = true
                        )
                    }
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

    private fun sendEvent(newEvent: WritePostUiEvent) = viewModelScope.launch {
        _uiEvent.emit(newEvent)
    }

}

private data class ViewModelState(
    val loadState: LoadStates = LoadStates.IDLE,
    val content: String = "",

    val errorMessage: ErrorMessage? = null,

    val isPostedSuccessfully: Boolean = false,
) {
    fun toPostUiState(): PostUiState {
        return if (isPostedSuccessfully) {
            PostUiState.Posted
        } else {
            PostUiState.WritingPost(
                content = content,
                loadState = loadState.action,
                errorMessage = errorMessage
            )
        }
    }
}

sealed interface PostUiState {
    data object Idle : PostUiState
    data class WritingPost(
        val content: String,
        val loadState: LoadState,
        val errorMessage: ErrorMessage? = null,
    ) : PostUiState
    data object Posted : PostUiState
}

sealed interface WritePostUiAction {
    data class OnContentChange(val content: String) : WritePostUiAction
    data object Submit : WritePostUiAction
    data object Reset : WritePostUiAction
}

sealed interface WritePostUiEvent {
    data class ShowSnack(val message: UiText) : WritePostUiEvent
    data class ShowToast(val message: UiText) : WritePostUiEvent
}

const val CONTENT = "content"
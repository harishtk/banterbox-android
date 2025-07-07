package space.banterbox.app.feature.home.presentation.landing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import space.banterbox.app.common.util.paging.PagedRequest
import space.banterbox.app.core.util.ErrorMessage
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.home.domain.model.Post
import space.banterbox.app.feature.home.domain.model.UserSummary
import space.banterbox.app.feature.home.domain.repository.PostRepository
import space.banterbox.app.feature.onboard.presentation.login.LoginUiEvent
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val viewModelState = MutableStateFlow(ViewModelState())

    val feedUiState = viewModelState
        .map(ViewModelState::toFeedUiState)
        .onStart { retryInternal(false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FeedUiState.Idle
        )

    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (HomeUiAction) -> Unit

    private var feedFetchJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }
    }

    private fun onUiAction(uiAction: HomeUiAction) {
        when (uiAction) {
            HomeUiAction.Refresh -> {
                retryInternal(false)
            }
        }
    }

    private fun retryInternal(loadMore: Boolean) {
        if (loadMore) {
            getGlobalFeed(LoadType.APPEND)
        } else {
            getGlobalFeed(LoadType.REFRESH)
        }
    }

    private fun getGlobalFeed(loadType: LoadType) {
        if (feedFetchJob?.isActive == true) {
            val t = IllegalStateException("Feed fetch job is already active, cancelling")
            Timber.w(t)
        }

        feedFetchJob?.cancel(CancellationException("New request"))
        setLoading(loadType, LoadState.Loading())

        val request = PagedRequest.create<Int>(
            loadType,
            key = viewModelState.value.nextPagingKey ?: 0,
            loadSize = if (loadType == LoadType.REFRESH) {
                FEED_PAGE_SIZE * 2
            } else {
                FEED_PAGE_SIZE
            }
        )

        feedFetchJob = viewModelScope.launch {
            when (val result = postRepository.globalFeed(request)) {
                Result.Loading -> {}
                is Result.Error -> {
                    Timber.e(result.exception)
                    viewModelState.update { state ->
                        state.copy(
                            errorMessage = ErrorMessage(
                                id = 0,
                                exception = result.exception,
                                message = UiText.somethingWentWrong
                            )
                        )
                    }
                    setLoading(loadType, LoadState.Error(result.exception))
                }
                is Result.Success -> {
                    viewModelState.update { state ->
                        state.copy(
                            posts = state.posts + result.data.posts,
                            users = state.users + result.data.users,
                            nextPagingKey = result.data.nextPagingKey,
                            endOfPaginationReached = result.data.nextPagingKey == null,
                            errorMessage = null
                        )
                    }
                    if (result.data.nextPagingKey != null) {
                        setLoading(loadType, LoadState.NotLoading.InComplete)
                    } else {
                        setLoading(loadType, LoadState.NotLoading.Complete)
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

    private fun sendEvent(newEvent: HomeUiEvent) = viewModelScope.launch {
        _uiEvent.emit(newEvent)
    }
}

private data class ViewModelState(
    val loadState: LoadStates = LoadStates.IDLE,
    val posts: List<Post> = emptyList(),
    val users: List<UserSummary> = emptyList(),

    val nextPagingKey: Int? = null,
    val endOfPaginationReached: Boolean = false,

    val errorMessage: ErrorMessage? = null,
) {
    fun toFeedUiState(): FeedUiState {
        if (posts.isNotEmpty()) {
            return FeedUiState.Success(posts, users)
        } else {
            when (loadState.refresh) {
                is LoadState.Loading -> {
                    return FeedUiState.Loading
                }
                is LoadState.Error -> {
                    val message = errorMessage ?: ErrorMessage.unknown()
                    return FeedUiState.Error(message)
                }
                else -> {
                    return FeedUiState.Idle
                }
            }
        }
    }
}

sealed interface FeedUiState {
    data object Loading : FeedUiState
    data object Idle : FeedUiState
    data class Error(val errorMessage: ErrorMessage) : FeedUiState
    data class Success(val posts: List<Post>, val users: List<UserSummary>) : FeedUiState
}

sealed interface HomeUiAction {
    data object Refresh : HomeUiAction
}

sealed interface HomeUiEvent {
    data class ShowToast(val message: UiText) : HomeUiEvent
    data class ShowSnackbar(val message: UiText) : HomeUiEvent
}

private const val FEED_PAGE_SIZE = 10

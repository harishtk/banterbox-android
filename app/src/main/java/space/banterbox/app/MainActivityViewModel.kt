package space.banterbox.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import space.banterbox.core.datastore.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import space.banterbox.app.core.domain.repository.UserDataRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
) : ViewModel() {

    private val userData = userDataRepository.userData
        .distinctUntilChanged { old, new ->
            old.userId == new.userId &&
                    old.onboardStep == new.onboardStep &&
                        old.serverUnderMaintenance == new.serverUnderMaintenance
        }

    val uiState: StateFlow<MainActivityUiState> = userData
        .map {
            Timber.d("UserData: $userData")
            when {
                // it.serverUnderMaintenance -> MainActivityUiState.Maintenance
                it.userId.isNotBlank() -> MainActivityUiState.Success(it)
                else -> MainActivityUiState.Login(it)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainActivityUiState.Loading
        )

}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    @Deprecated("Not yet implemented")
    data object Maintenance : MainActivityUiState
    data class Login(val data: UserData) : MainActivityUiState
    data class Success(val data: UserData) : MainActivityUiState
}
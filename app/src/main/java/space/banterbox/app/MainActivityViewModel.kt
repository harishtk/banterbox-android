package space.banterbox.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepul.shops.core.datastore.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import space.banterbox.app.core.domain.repository.ShopDataRepository
import space.banterbox.app.core.domain.repository.UserDataRepository
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    shopDataRepository: ShopDataRepository,
) : ViewModel() {

    private val userData = userDataRepository.userData
        .distinctUntilChanged { old, new ->
            old.userId == new.userId &&
                    old.onboardStep == new.onboardStep &&
                        old.serverUnderMaintenance == new.serverUnderMaintenance
        }

    private val shopData = shopDataRepository.shopData
        .distinctUntilChanged()

    val uiState: StateFlow<MainActivityUiState> = userData
        .map {
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
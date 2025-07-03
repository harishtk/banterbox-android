package space.banterbox.app.feature.home.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepul.shops.core.analytics.Analytics
import com.pepul.shops.core.analytics.AnalyticsLogger
import com.pepul.shops.core.datastore.UserData
import space.banterbox.app.R
import space.banterbox.app.common.util.UiText
import space.banterbox.app.core.domain.repository.UserDataRepository
import space.banterbox.app.core.domain.usecase.LogoutUseCase
import space.banterbox.app.core.domain.usecase.UserAuthStateUseCase
import space.banterbox.app.feature.home.presentation.util.SettingsItem
import space.banterbox.app.feature.home.presentation.util.SettingsListType
import space.banterbox.app.ifDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

enum class SettingsIds(val id: Int) {
    ChangePinCode(0), Faq(1), Feedback(2), HelpAndSupport(3), About(4),
    Terms(5), Logout(6), Unknown(-1);

    companion object {
        fun fromId(id: Int): SettingsIds {
            return values().firstOrNull() { it.id == id }
                ?: Unknown
        }
    }
}

data class SettingsItemWithAuthState(
    val settingsItem: SettingsItem,
    val requiresLogin: Boolean,
)

val settingsListData = listOf(
    SettingsItemWithAuthState(
        settingsItem = SettingsItem(
            settingsListType = SettingsListType.SIMPLE,
            id = SettingsIds.ChangePinCode.id,
            title = UiText.DynamicString("Change Pin Code"),
            icon = null, /*R.drawable.ic_location_pin,*/
            description = null,
            hasMore = true,
        ),
        requiresLogin = true
    ),
    /*SettingsItemWithAuthState(
        settingsItem = SettingsItem(
            settingsListType = SettingsListType.SIMPLE,
            id = SettingsIds.Faq.id,
            title = UiText.DynamicString("FAQs"),
            icon = R.drawable.ic_faq_outline,
            description = UiText.DynamicString("Frequently asked questions"),
            true
        ),
        requiresLogin = false
    ),
    SettingsItemWithAuthState(
        settingsItem = SettingsItem(
            settingsListType = SettingsListType.SIMPLE,
            id = SettingsIds.Feedback.id,
            title = UiText.DynamicString("Feedback"),
            icon = R.drawable.ic_feedback_outline,
            description = UiText.DynamicString("Tell us something you like"),
            true
        ),
        requiresLogin = true
    ),*/
    SettingsItemWithAuthState(
        settingsItem = SettingsItem(
            settingsListType = SettingsListType.SIMPLE,
            id = SettingsIds.HelpAndSupport.id,
            title = UiText.DynamicString("Help & Support"),
            icon = null, /*R.drawable.ic_helpline_outline,*/
            description = UiText.DynamicString("Our experts will guide you"),
            true
        ),
        requiresLogin = false
    ),
    SettingsItemWithAuthState(
        settingsItem = SettingsItem(
            settingsListType = SettingsListType.SIMPLE,
            id = SettingsIds.About.id,
            title = UiText.DynamicString("About"),
            icon = null, /*R.drawable.ic_info_outline,*/
            description = UiText.DynamicString("Know more about us"),
            true
        ),
        requiresLogin = false
    ),
    SettingsItemWithAuthState(
        settingsItem = SettingsItem(
            settingsListType = SettingsListType.SIMPLE,
            id = SettingsIds.Terms.id,
            title = UiText.StringResource(R.string.terms_and_conditions),
            icon = null, /*R.drawable.ic_terms_outline,*/
            description = null,
            true
        ),
        requiresLogin = false
    ),
    // SettingsItem(settingsListType = SettingsListType.SIMPLE, id = 5, title = "Delete my account", R.drawable.ic_info_outline, "Want out? But We will miss you.", true),
    SettingsItemWithAuthState(
        settingsItem = SettingsItem(
            settingsListType = SettingsListType.SIMPLE,
            id = SettingsIds.Logout.id,
            title = UiText.DynamicString("Logout"),
            icon = null, /*R.drawable.ic_logout_outline,*/
            description = null,
            hasMore = false
        ),
        requiresLogin = true
    ),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    userAuthStateUseCase: UserAuthStateUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val analyticsLogger: AnalyticsLogger,
) : ViewModel() {

    private val userData = userDataRepository.userData
        .distinctUntilChanged()

    private val viewModelState = MutableStateFlow(ViewModelState())
    val uiState: StateFlow<SettingsUiState> = viewModelState
        .map(ViewModelState::toSettingsUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = viewModelState.value.toSettingsUiState()
        )

    private val _toastText: MutableSharedFlow<UiText?> = MutableSharedFlow(0)
    val toastText = _toastText.asSharedFlow()

    init {
        userDataRepository.userData
            .distinctUntilChanged()
            .map { userData ->
                val authenticated = userData.userId?.isNotBlank() == true
                settingsListData
                    .filter {
                        !it.requiresLogin || it.requiresLogin == authenticated
                    }
                    .map { settingsListData ->
                        if (settingsListData.settingsItem.id == SettingsIds.ChangePinCode.id) {
                            val description = userData?.pinCode?.let { code ->
                                UiText.StringResource(R.string.your_pin_code, code)
                            } ?: UiText.emptyString
                            settingsListData.settingsItem.copy(description = description)
                        } else {
                            settingsListData.settingsItem
                        }
                    }
            }
            .onEach { settingsItems ->
                viewModelState.update { state ->
                    state.copy(
                        settingsItems = settingsItems
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    val settingsProfileState: StateFlow<SettingsProfileState> = userData
        .map { _userData ->
            if (_userData == null) {
                SettingsProfileState.Login
            } else {
                SettingsProfileState.ProfileData(
                    userData = _userData
                )
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsProfileState.Loading
        )

    @Suppress("unused")
    @Deprecated("unused")
    val settingsItemsOld: StateFlow<List<SettingsItem>> =
        userAuthStateUseCase()
            .distinctUntilChanged()
            .map { authState ->
                settingsListData
                    .filter {
                        !it.requiresLogin || it.requiresLogin == authState.isAuthenticated()
                    }
                    .map { it.settingsItem }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun reset() {
        viewModelState.update { state ->
            state.copy(
                loading = false,
                uiErrorText = null,
            )
        }
    }

    fun logout() {
        setLoading(true)
        viewModelScope.launch {
            /* Logs out the user */
            kotlin.runCatching {
                showToast(UiText.DynamicString("You're logged out!"))
                logoutUseCase()
            }
                .onSuccess {
                    analyticsLogger.logEvent(Analytics.Event.SETTINGS_LOGOUT_CLICK)
                    analyticsLogger.setUserId(null)
                    setLoading(false)
                }
                .onFailure { t ->
                    ifDebug { Timber.e(t) }
                    viewModelState.update { state ->
                        state.copy(
                            uiErrorText = UiText.DynamicString("Unable to process the request")
                        )
                    }
                    setLoading(false)
                }
        }
    }

    private fun setLoading(loading: Boolean) {
        viewModelState.update { state -> state.copy(loading = loading) }
    }

    private fun showToast(text: UiText) = viewModelScope.launch {
        _toastText.emit(text)
    }
}

private data class ViewModelState(
    val loading: Boolean = false,
    val uiErrorText: UiText? = null,

    val settingsItems: List<SettingsItem> = emptyList(),
) {
    fun toSettingsUiState(): SettingsUiState {
        return if (settingsItems.isEmpty()) {
            SettingsUiState.Loading
        } else {
            SettingsUiState.SettingsList(
                settingsItems = settingsItems,
                loading = loading,
                uiErrorText = uiErrorText
            )
        }
    }
}

interface SettingsUiState {
    data object Loading : SettingsUiState

    data class SettingsList(
        val settingsItems: List<SettingsItem>,
        val loading: Boolean,
        val uiErrorText: UiText?,
    ) : SettingsUiState {
        override fun toString(): String {
            return "SettingsList(settingItems=${settingsItems.size},loading=$loading,uiErrorText=$uiErrorText)"
        }
    }
}

sealed interface SettingsProfileState {
    data object Loading : SettingsProfileState

    data object Login : SettingsProfileState

    data class ProfileData(
        val userData: UserData,
    ) : SettingsProfileState
}
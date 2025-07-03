package space.banterbox.app.feature.onboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class OnboardSharedViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _signInData = MutableStateFlow<SignInData>(SignInData())
    val signInData: StateFlow<SignInData> = _signInData.asStateFlow()

    val phoneNumber = savedStateHandle.getStateFlow("phone", "")
    val countryCode = savedStateHandle.getStateFlow("country_code", "")
    val accountType = savedStateHandle.getStateFlow("account_type", "")


    fun setAccountData(
        phone: String,
        countryCode: String,
        accountType: String = "new",
    ) {
        savedStateHandle["phone"] = phone
        savedStateHandle["country_code"] = countryCode
        savedStateHandle["account_type"] = accountType

        _signInData.update { state ->
            state.copy(
                phone = phone, countryCode = countryCode, accountType = accountType
            )
        }
    }
}

data class SignInData(
    val phone: String = "",
    val countryCode: String = "",
    val accountType: String = "",
)
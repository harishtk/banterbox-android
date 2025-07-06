package space.banterbox.app.feature.home.presentation.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.banterbox.app.common.util.UiText
import space.banterbox.app.common.util.loadstate.LoadStates
import space.banterbox.app.core.domain.repository.UserDataRepository
import space.banterbox.app.core.util.ErrorMessage
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
) : ViewModel() {


}

private data class ViewModelState(
    val loadState: LoadStates = LoadStates.IDLE,

    val exception: Exception? = null,
    val uiErrorMessage: UiText? = null,
)
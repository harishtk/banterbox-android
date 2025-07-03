package space.banterbox.app.feature.onboard.presentation.util

import androidx.compose.ui.focus.FocusRequester
import space.banterbox.app.core.designsystem.component.text.TextFieldState
import space.banterbox.app.core.designsystem.component.text.textFieldStateSaver

class StoreNameState(
    val name: String,
    val focusRequester: FocusRequester? = null,
)
    : TextFieldState(validator = ::isNameValid, errorFor = ::storeNameValidationError) {
    init {
        text = name
    }
}

private fun storeNameValidationError(name: String): String {
    if (name.trim().isBlank()) {
        return "Cannot be empty"
    }
    if (name.trim().contains("\$^*|\\\\/<>:;'\"!~`")) {
        return "Cannot contain special characters"
    }
    return "Field is required"
}

private fun isNameValid(name: String): Boolean {
    // TODO: handle store name validation here
    return name.isNotBlank() &&
            !name.contains("\$^*|\\\\/<>:;'\"!~`")
}

val StoreNameStateSaver = textFieldStateSaver(StoreNameState(""))
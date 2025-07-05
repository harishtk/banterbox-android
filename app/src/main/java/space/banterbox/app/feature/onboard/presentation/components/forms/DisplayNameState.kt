package space.banterbox.app.feature.onboard.presentation.components.forms

import space.banterbox.app.core.designsystem.component.text.TextFieldState
import space.banterbox.app.core.designsystem.component.text.textFieldStateSaver

class DisplayNameState(
    initialValue: String = ""
) : TextFieldState(validator = ::isValidName, errorFor = ::displayNameError) {

    init {
        text = initialValue
    }
}

private fun isValidName(name: String): Boolean {
    return name.isNotBlank() && name.length in 4..30
}

private fun displayNameError(name: String): String {
    return when {
        name.isBlank() -> "Display name cannot be blank"
        name.length < 4 -> "Display name must be at least 4 characters"
        name.length > 30 -> "Display name must not exceed 30 characters"
        else -> ""
    }
}

val DisplayNameStateSaver = textFieldStateSaver(DisplayNameState())
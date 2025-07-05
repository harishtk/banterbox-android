package space.banterbox.app.core.designsystem.component.forms

import space.banterbox.app.Constant

class UsernameFieldState(private val initialUsername: String) :
    space.banterbox.app.core.designsystem.component.text.TextFieldState(validator = ::isValidUsername, errorFor = ::usernameErrorMessage) {

    init {
        this.text = initialUsername
    }

}

private fun isValidUsername(username: String): Boolean {
    return username.isNotBlank()
}

private fun usernameErrorMessage(username: String): String {
    return when {
        username.isBlank() -> "Enter a username"
        else -> ""
    }
}

val UsernameFieldStateSaver =
    _root_ide_package_.space.banterbox.app.core.designsystem.component.text.textFieldStateSaver(
        UsernameFieldState("")
    )
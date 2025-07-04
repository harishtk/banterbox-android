package space.banterbox.app.core.designsystem.component.text

import space.banterbox.app.Constant

class UsernameFieldState(private val initialUsername: String) :
    TextFieldState(validator = ::isValidUsername, errorFor = ::usernameErrorMessage) {

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

val UsernameFieldStateSaver = textFieldStateSaver(UsernameFieldState(""))
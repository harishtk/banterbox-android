package space.banterbox.app.core.designsystem.component.text

class PasswordFieldState() :
    TextFieldState(validator = ::isValidPassword, errorFor = ::usernameErrorMessage)

private fun isValidPassword(password: String): Boolean {
    return password.isNotBlank() &&
            password.length >= 6
}

private fun usernameErrorMessage(password: String): String {
    return when {
        password.isBlank() -> "Enter a password"
        password.length < 6 -> "Password is too short"
        else -> "Password contains invalid characters"
    }
}
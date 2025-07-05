package space.banterbox.app.feature.onboard.presentation.components.forms

import space.banterbox.app.core.designsystem.component.text.PasswordFieldState
import space.banterbox.app.core.designsystem.component.text.TextFieldState

class ConfirmPasswordState(
    private val passwordFieldState: PasswordFieldState,
) : TextFieldState(errorFor = ::confirmPasswordErrorMessage) {

    override val isValid: Boolean
        get() = isValidConfirmPassword(passwordFieldState.text, text)
}

private fun isValidConfirmPassword(originalPassword: String, confirmPassword: String): Boolean {
    return when {
        confirmPassword.isBlank() -> false
        confirmPassword != originalPassword -> false
        else -> true

    }
}

private fun confirmPasswordErrorMessage(confirmPassword: String): String {
    return "Passwords do not match"
}
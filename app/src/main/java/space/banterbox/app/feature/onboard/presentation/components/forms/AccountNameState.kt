package space.banterbox.app.feature.onboard.presentation.components.forms

import android.util.Patterns
import space.banterbox.app.core.designsystem.component.text.TextFieldState
import space.banterbox.app.core.designsystem.component.text.textFieldStateSaver

private val accountNamePattern = Regex("^[a-zA-Z]+[\\w_\\s]+$")

class AccountNameState(accountName: String)
    : TextFieldState(validator = ::isValidAccountHolderName, errorFor = ::accountNameErrorMessage) {
    init {
        text = accountName
    }
}

private fun accountNameErrorMessage(accountName: String): String {
    return when {
        accountName.isBlank() -> "Account Name cannot be blank"
        else -> "Enter a valid Account Name"
    }
}

private fun isValidAccountHolderName(number: String): Boolean {
    return number.isNotBlank() && number.matches(accountNamePattern)
}

val AccountNameStateSaver = textFieldStateSaver(AccountNameState(""))
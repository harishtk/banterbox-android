package space.banterbox.app.feature.onboard.presentation.components.forms

import space.banterbox.app.core.designsystem.component.text.TextFieldState
import space.banterbox.app.core.designsystem.component.text.textFieldStateSaver

private val accountNumberPattern = Regex("^[0-9]{11,16}$")

class AccountNumberState(accountNumber: String)
    : TextFieldState(validator = ::isValidAccountNumber, errorFor = ::accountNumberErrorMessage) {
        init {
            text = accountNumber
        }
}

private fun accountNumberErrorMessage(accountNumber: String): String {
    if (accountNumber.isBlank()) {
        return "Account Number cannot be blank"
    }
    return if (accountNumber.length < 11) {
        "Account Number at least contains 11 digits"
    } else {
        "Account Number cannot exceed 16 digits"
    }
}

private fun isValidAccountNumber(number: String): Boolean {
    return number.matches(accountNumberPattern)
}

val AccountNumberStateSaver = textFieldStateSaver(AccountNumberState(""))
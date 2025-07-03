package space.banterbox.app.feature.onboard.presentation.components.forms

import space.banterbox.app.core.designsystem.component.text.TextFieldState
import space.banterbox.app.core.designsystem.component.text.textFieldStateSaver

private val ifscNumberPattern = Regex("^[A-Za-z]{4}[a-zA-Z0-9]{7}$")

class IfscState(ifscNumber: String)
    : TextFieldState(validator = ::isValidIfsc, errorFor = ::ifscNumberErrorMessage) {
    init {
        text = ifscNumber
    }
}

private fun ifscNumberErrorMessage(ifscNumber: String): String {
    return when {
        ifscNumber.isBlank() -> "IFSC Code cannot be empty"

        else -> "Enter a valid IFSC Code"
    }
}

private fun isValidIfsc(number: String): Boolean {
    return number.isNotBlank() && number.matches(ifscNumberPattern)
}

val IfscStateSaver = textFieldStateSaver(IfscState(""))
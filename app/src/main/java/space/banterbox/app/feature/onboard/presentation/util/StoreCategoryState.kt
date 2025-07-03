package space.banterbox.app.feature.onboard.presentation.util

import space.banterbox.app.core.designsystem.component.text.TextFieldState
import space.banterbox.app.core.designsystem.component.text.textFieldStateSaver

class StoreCategoryState(
    private val name: String
)
    : TextFieldState(validator = ::isCategoryValid, errorFor = ::categoryValidationError) {
    init {
        text = name
    }
}

private fun categoryValidationError(text: String): String {
    if (text.trim().isBlank()) {
        return "Field is required"
    }
    return ""
}

private fun isCategoryValid(name: String): Boolean {
    return name.isNotBlank()
}

val StoreCategoryStateSaver = textFieldStateSaver(StoreCategoryState(""))
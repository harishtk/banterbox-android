package space.banterbox.app.feature.onboard.presentation.util.form

import space.banterbox.app.common.util.UiText

class ValidationException(
    type: Type = Type.Default,
    key: String = NO_KEY,
    uiText: UiText = DefaultErrorText,
) : Exception() {

    enum class Type {
        Default, Input;
    }

    companion object {
        const val NO_KEY = ""
        val DefaultErrorText = UiText.DynamicString("An error occurred")
    }
}
package space.banterbox.app.core.util

import space.banterbox.app.common.util.UiText

data class ValidationResult(
    val typedValue: String, /* for StateFlow to recognize different value */
    val successful: Boolean = false,
    val errorMessage: UiText? = null
)
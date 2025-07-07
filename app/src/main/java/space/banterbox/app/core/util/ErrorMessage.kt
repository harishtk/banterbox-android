package space.banterbox.app.core.util

import space.banterbox.app.common.util.UiText

data class ErrorMessage(
    val id: Long,
    val exception: Throwable?,
    val message: UiText?
) {
    companion object {
        fun unknown() = ErrorMessage(
            id = 0,
            exception = null,
            message = UiText.somethingWentWrong
        )
    }
}

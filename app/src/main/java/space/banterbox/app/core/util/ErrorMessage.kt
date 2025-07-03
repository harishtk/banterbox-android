package space.banterbox.app.core.util

import space.banterbox.app.common.util.UiText

data class ErrorMessage(
    val id: Long,
    val exception: Throwable?,
    val message: UiText?
)

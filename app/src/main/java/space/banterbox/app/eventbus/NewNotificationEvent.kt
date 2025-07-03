package space.banterbox.app.eventbus

data class NewNotificationEvent(
    val hint: String,
    val timestamp: Long
)
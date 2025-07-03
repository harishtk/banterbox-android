package .app.eventbus

data class NewNotificationEvent(
    val hint: String,
    val timestamp: Long
)
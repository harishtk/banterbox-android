package space.banterbox.app.feature.home.domain.model

data class BanterboxNotification(
    val id: String,
    val type: BanterboxNotificationType,
    val subType: String?,
    val message: String,
    val referenceId: String?,
    val createdAt: String,
    val read: Boolean,
    val recipientId: String,
    val actorId: String?,
    val actor: UserSummary,
)

enum class BanterboxNotificationType {
    FOLLOW, LIKE, GENERAL, ANNOUNCEMENTS, UNKNOWN;

    companion object {
        fun fromString(type: String): BanterboxNotificationType {
            return when (type) {
                "FOLLOW" -> FOLLOW
                "LIKE" -> LIKE
                "GENERAL" -> GENERAL
                "ANNOUNCEMENTS" -> ANNOUNCEMENTS
                else -> UNKNOWN
            }
        }
    }
}
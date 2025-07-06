package space.banterbox.app.feature.home.domain.model

data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val profilePictureId: String,
    val createdAt: String,
)

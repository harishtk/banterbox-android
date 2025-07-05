package space.banterbox.app.core.domain.model

data class LoginUser(
    val userId: String,
    val username: String,
    val profileName: String,
    val bio: String,
    val profileImage: String,
    val createdAt: String,
    val followersCount: Int,
    val followingCount: Int,
    val postsCount: Int,
    val notificationCount: Int = 0,
    val onboardStep: String = ""
)

package space.banterbox.app.feature.onboard.domain.model.request

data class SignupRequest(
    val username: String,
    val password: String,
    val displayName: String,
    val bio: String
)
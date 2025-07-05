package space.banterbox.app.feature.onboard.domain.model

import space.banterbox.app.core.domain.model.LoginUser

data class LoginData(
    val loginUser: LoginUser?,
    val deviceToken: String?,
    val refreshToken: String?,
    val showProfile: Boolean = false,
) {
    companion object {
        fun empty() = LoginData(
            loginUser = null,
            deviceToken = null,
            refreshToken = null,
        )
    }
}
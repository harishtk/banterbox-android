package space.banterbox.app.feature.onboard.domain.model

import space.banterbox.app.core.domain.model.LoginUser

data class LoginData(
    val loginUser: LoginUser?,
    val deviceToken: String?,
    val refreshToken: String?,
)
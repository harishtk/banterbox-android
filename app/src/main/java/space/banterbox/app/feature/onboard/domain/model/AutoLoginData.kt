package space.banterbox.app.feature.onboard.domain.model

import space.banterbox.app.core.domain.model.LoginUser

data class AutoLoginData(
    val forceUpdate: Boolean,
    val maintenance: Boolean,
    val loginUser: LoginUser?
)
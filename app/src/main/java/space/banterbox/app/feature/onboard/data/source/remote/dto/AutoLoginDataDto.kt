package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.AutoLoginData

data class AutoLoginDataDto(
    @SerializedName("forceUpdate")
    val forceUpdate: Boolean,
    @SerializedName("loginUser")
    val loginUserDto: LoginUserDto?,
    @SerializedName("maintenance")
    val maintenance: Boolean?,
)

fun AutoLoginDataDto.toAutoLoginData(): AutoLoginData {
    return AutoLoginData(
        forceUpdate = forceUpdate,
        loginUser = loginUserDto?.toLoginUser(),
        maintenance = this.maintenance ?: false,

    )
}
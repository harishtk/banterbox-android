
package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.request.LoginRequest
import space.banterbox.app.nullAsEmpty

data class LoginRequestDto(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String,
)

fun LoginRequest.asDto(): LoginRequestDto {
    return LoginRequestDto(
        username = username,
        password = password
    )
}
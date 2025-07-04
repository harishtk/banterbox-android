package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.request.SignupRequest

data class SignupRequestDto(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("displayName")
    val displayName: String,
    @SerializedName("bio")
    val bio: String
)

fun SignupRequest.asDto(): SignupRequestDto {
    return SignupRequestDto(
        username = username,
        password = password,
        displayName = displayName,
        bio = bio
    )
}
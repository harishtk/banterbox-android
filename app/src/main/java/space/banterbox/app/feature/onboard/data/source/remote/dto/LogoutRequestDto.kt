package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.request.LogoutRequest

data class LogoutRequestDto(
    @SerializedName("userId")
    val userId: String
)

fun LogoutRequest.asDto(): LogoutRequestDto {
    return LogoutRequestDto(
        userId = userId
    )
}
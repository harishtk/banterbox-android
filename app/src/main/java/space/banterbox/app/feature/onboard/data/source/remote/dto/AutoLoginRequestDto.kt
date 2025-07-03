package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.request.AutoLoginRequest

data class AutoLoginRequestDto(
    @SerializedName("timestamp")
    val timestamp: Long
)

fun AutoLoginRequest.asDto(): AutoLoginRequestDto {
    return AutoLoginRequestDto(
        timestamp = timestamp
    )
}
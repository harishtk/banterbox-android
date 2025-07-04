package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequestDto(
    @SerializedName("refreshToken")
    val refreshToken: String,
)

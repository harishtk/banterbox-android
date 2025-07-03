package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName

data class LaunchStoreRequestDto(
    @SerializedName("timestamp")
    val timestamp: Long,
)

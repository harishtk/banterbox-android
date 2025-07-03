package space.banterbox.app.feature.onboard.data.source.remote.model

import space.banterbox.app.feature.onboard.data.source.remote.dto.AutoLoginDataDto
import com.google.gson.annotations.SerializedName

data class AutoLoginResponse(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: AutoLoginDataDto?
)


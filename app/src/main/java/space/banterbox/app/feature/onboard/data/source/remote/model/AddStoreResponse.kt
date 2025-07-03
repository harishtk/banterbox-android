package space.banterbox.app.feature.onboard.data.source.remote.model

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.data.source.remote.dto.LoginDataDto

data class AddStoreResponse(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: LoginDataDto?
)

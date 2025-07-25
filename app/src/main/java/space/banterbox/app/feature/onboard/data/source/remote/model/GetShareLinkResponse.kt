package space.banterbox.app.feature.onboard.data.source.remote.model

import space.banterbox.app.feature.onboard.data.source.remote.dto.ShareLinkDataDto
import com.google.gson.annotations.SerializedName

data class GetShareLinkResponse(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: ShareLinkDataDto?
)


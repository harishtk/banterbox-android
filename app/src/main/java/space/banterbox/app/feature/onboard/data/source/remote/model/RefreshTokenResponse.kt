package space.banterbox.app.feature.onboard.data.source.remote.model

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Data?
) {
    data class Data(
        @SerializedName("accessToken")
        val accessToken: String,
        @SerializedName("refreshToken")
        val refreshToken: String,
    )
}

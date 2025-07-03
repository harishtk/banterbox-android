package space.banterbox.app.feature.onboard.data.source.remote.model

import com.google.gson.annotations.SerializedName

data class LaunchStoreResponse(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Data?
) {
    data class Data(
        @SerializedName("onboardStep")
        val onboardStep: String,
    )
}

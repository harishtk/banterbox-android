package space.banterbox.app.feature.onboard.data.source.remote.model

import com.google.gson.annotations.SerializedName

data class AddBankResponse(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: AddBankDataDto?
) {
    data class AddBankDataDto(
        @SerializedName("onboardStep")
        val onboardStep: String
    )
}

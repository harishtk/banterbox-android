package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.SavePinCodeRequest

data class SavePinCodeRequestDto(
    @SerializedName("pincode")
    val pinCode: String,
    @SerializedName("area")
    val area: String,
)

fun SavePinCodeRequest.asDto(): SavePinCodeRequestDto {
    return SavePinCodeRequestDto(
        pinCode = pinCode,
        area = area
    )
}

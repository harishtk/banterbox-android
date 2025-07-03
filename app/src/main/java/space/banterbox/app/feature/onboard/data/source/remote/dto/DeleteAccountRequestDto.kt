
package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.DeleteAccountRequest
import space.banterbox.app.nullAsEmpty

data class DeleteAccountRequestDto(
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("countryCode")
    val countryCode: String,
    @SerializedName("callFor")
    val callFor: String,
    @SerializedName("reason")
    val reason: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("otp")
    val otp: String?,
    @SerializedName("isResend")
val isResend: Boolean,
)

fun DeleteAccountRequest.asDto(): DeleteAccountRequestDto {
    return DeleteAccountRequestDto(
        phoneNumber = phoneNumber,
        countryCode = countryCode,
        callFor = callFor,
        reason = reason,
        description = description.nullAsEmpty(),
        otp = otp.nullAsEmpty(),
        isResend = isResend
    )
}



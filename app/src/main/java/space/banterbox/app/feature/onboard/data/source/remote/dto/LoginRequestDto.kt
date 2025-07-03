
package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.request.LoginRequest
import space.banterbox.app.nullAsEmpty

data class LoginRequestDto(
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("countryCode")
    val countryCode: String,
    @SerializedName("guestUserId")
    val guestUserId: Long,
    @SerializedName("callFor")
    val callFor: String,
    @SerializedName("otp")
    val otp: String,
    @SerializedName("device")
    val platform: String,
    @SerializedName("fcm")
    val fcm: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("recaptchaToken")
    val recaptchaToken: String,
    @SerializedName("isResend")
    val isResend: Boolean,
    @SerializedName("utm_campaign")
    val utmCampaign: String?,
    @SerializedName("utm_medium")
    val utmMedium: String?,
)

fun LoginRequest.asDto(): LoginRequestDto {
    return LoginRequestDto(
        phoneNumber = phoneNumber,
        countryCode = countryCode,
        callFor = callFor,
        guestUserId = guestUserId,
        otp = otp.nullAsEmpty(),
        platform = platform,
        fcm = fcm,
        type = type,
        recaptchaToken = recaptchaToken.nullAsEmpty(),
        isResend = isResend,
        utmCampaign = this.utmCampaign,
        utmMedium = this.utmMedium,
    )
}
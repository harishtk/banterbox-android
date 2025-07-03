
package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.request.SocialLoginRequest

data class SocialLoginRequestDto(
    @SerializedName("accountType")
    val accountType: String,
    @SerializedName("accountId")
    val accountId: String,
    @SerializedName("emailAddress")
    val email: String,
    @SerializedName("guestUserId")
    val guestUserId: Long,
    @SerializedName("device")
    val platform: String,
    @SerializedName("fcm")
    val fcm: String
)

fun SocialLoginRequest.asDto(): SocialLoginRequestDto {
    return SocialLoginRequestDto(
        accountType = accountType,
        accountId = accountId,
        email = email,
        guestUserId = guestUserId,
        platform = platform,
        fcm = fcm
    )
}

package space.banterbox.app.feature.onboard.domain.model.request

data class LoginRequest(
    val phoneNumber: String,
    val countryCode: String,
    val guestUserId: Long,
    val callFor: String,
    val platform: String,
    val fcm: String,
    val type: String,
) {
    var otp:            String? = null
    var utmMedium:      String? = null
    var utmCampaign:    String? = null
    var recaptchaToken:   String? = null
    var isResend:       Boolean = false
}


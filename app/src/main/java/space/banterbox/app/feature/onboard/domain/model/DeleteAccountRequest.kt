package space.banterbox.app.feature.onboard.domain.model

data class DeleteAccountRequest(
    val phoneNumber: String,
    val countryCode: String,
    val callFor: String,
    val reason: String,
) {
    var description: String?    = null
    var otp: String?            = null
    var isResend: Boolean       = false
}
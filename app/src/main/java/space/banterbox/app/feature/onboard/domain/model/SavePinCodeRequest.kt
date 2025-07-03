package space.banterbox.app.feature.onboard.domain.model

data class SavePinCodeRequest(
    val pinCode: String,
    val area: String /* division */
)


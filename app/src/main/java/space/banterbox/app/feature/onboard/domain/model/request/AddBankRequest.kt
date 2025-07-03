package space.banterbox.app.feature.onboard.domain.model.request

data class AddBankRequest(
    val accountNumber: String,
    val accountHolderName: String,
    val ifscCode: String,
)
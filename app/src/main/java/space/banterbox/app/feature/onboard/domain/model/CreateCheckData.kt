package space.banterbox.app.feature.onboard.domain.model

data class CreateCheckData(
    val allowModelCreate: Boolean,
    val siteDown: Boolean,
    val pendingModelPayment: Boolean
)
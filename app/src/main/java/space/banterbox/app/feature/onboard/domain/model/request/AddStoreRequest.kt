package space.banterbox.app.feature.onboard.domain.model.request

data class AddStoreRequest(
    val tempId: String,
    val storeName: String,
    val storeCategory: String,
    val fcm: String,
)

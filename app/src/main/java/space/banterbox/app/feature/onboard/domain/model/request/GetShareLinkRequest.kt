package space.banterbox.app.feature.onboard.domain.model.request

data class GetShareLinkRequest(
    val modelId: String,
    val avatarId: String,
    val folderName: String,
    val fileName: String
)
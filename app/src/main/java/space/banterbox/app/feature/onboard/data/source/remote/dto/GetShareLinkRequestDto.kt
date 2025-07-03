package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.request.GetShareLinkRequest

data class GetShareLinkRequestDto(
    @SerializedName("modelId")
    val modelId: String,
    @SerializedName("avatarId")
    val avatarId: String,
    @SerializedName("folderName")
    val folderName: String,
    @SerializedName("fileName")
    val fileName: String
)

fun GetShareLinkRequest.asDto(): GetShareLinkRequestDto {
    return GetShareLinkRequestDto(
        modelId = modelId,
        avatarId = avatarId,
        folderName = folderName,
        fileName = fileName
    )
}
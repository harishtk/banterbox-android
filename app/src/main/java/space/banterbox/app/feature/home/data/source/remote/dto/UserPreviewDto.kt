package space.banterbox.app.feature.home.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.home.domain.model.UserPreview

data class UserPreviewDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("displayName")
    val displayName: String,
    @SerializedName("profilePictureId")
    val profilePictureId: String,
)

fun UserPreviewDto.toUserPreview(): UserPreview {
    return UserPreview(
        id = id,
        username = username,
        displayName = displayName,
        profilePictureId = profilePictureId,
    )
}
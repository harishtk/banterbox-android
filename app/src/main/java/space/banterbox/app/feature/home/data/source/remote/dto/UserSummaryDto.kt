package space.banterbox.app.feature.home.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.home.domain.model.UserSummary

data class UserSummaryDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("displayName")
    val displayName: String,
    @SerializedName("profilePictureId")
    val profilePictureId: String?,
    @SerializedName("isFollowing")
    val isFollowing: Boolean?,
)

fun UserSummaryDto.toUserSummary(): UserSummary {
    return UserSummary(
        id = id,
        username = username,
        displayName = displayName,
        profilePictureId = profilePictureId ?: "",
        isFollowing = isFollowing ?: false,
    )
}
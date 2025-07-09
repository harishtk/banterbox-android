package space.banterbox.app.feature.home.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.home.domain.model.BanterboxNotification
import space.banterbox.app.feature.home.domain.model.BanterboxNotificationType

data class NotificationDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("subType") val subType: String?,
    @SerializedName("message") val message: String,
    @SerializedName("referenceId") val referenceId: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("read") val read: Boolean,
    @SerializedName("recipientId") val recipientId: String,
    @SerializedName("actorId") val actorId: String,
    @SerializedName("actor") val actor: UserSummaryDto
)

fun NotificationDto.toBanterboxNotification(): BanterboxNotification {
    return BanterboxNotification(
        id = id,
        type = BanterboxNotificationType.fromString(type),
        subType = subType,
        message = message ?: "",
        referenceId = referenceId,
        createdAt = createdAt,
        read = read,
        recipientId = recipientId,
        actorId = actorId,
        actor = actor.toUserSummary(),
    )
}
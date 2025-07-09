package space.banterbox.app.feature.home.domain.model

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.home.data.source.remote.dto.NotificationDto

data class NotificationData(
    val notifications: List<BanterboxNotification>,
    val page: Int,
    val pageSize: Int,
    val totalNotifications: Int,
    val totalPages: Int,
    val isLastPage: Boolean
)

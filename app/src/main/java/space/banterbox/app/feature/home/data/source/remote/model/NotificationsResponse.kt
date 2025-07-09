package space.banterbox.app.feature.home.data.source.remote.model

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.home.data.source.remote.dto.NotificationDto
import space.banterbox.app.feature.home.data.source.remote.dto.toBanterboxNotification
import space.banterbox.app.feature.home.domain.model.NotificationData

data class NotificationsResponse(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Data?
) {
    data class Data(
        @SerializedName("notifications")
        val notifications: List<NotificationDto>,
        @SerializedName("page")
        val page: Int,
        @SerializedName("pageSize")
        val pageSize: Int,
        @SerializedName("totalNotifications")
        val totalNotifications: Int,
        @SerializedName("totalPages")
        val totalPages: Int,
        @SerializedName("isLastPage")
        val isLastPage: Boolean
    )
}

fun NotificationsResponse.Data.toNotificationData(): NotificationData {
    return NotificationData(
        notifications = notifications.map(NotificationDto::toBanterboxNotification),
        page = page,
        pageSize = pageSize,
        totalNotifications = totalNotifications,
        totalPages = totalPages,
        isLastPage = isLastPage
    )
}

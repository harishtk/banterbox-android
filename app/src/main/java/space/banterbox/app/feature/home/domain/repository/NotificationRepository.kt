package space.banterbox.app.feature.home.domain.repository

import kotlinx.coroutines.flow.Flow
import space.banterbox.app.common.util.paging.PagedData
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.home.domain.model.BanterboxNotification
import space.banterbox.app.feature.home.domain.model.NotificationData
import space.banterbox.app.feature.home.domain.model.request.NotificationRequest

interface NotificationRepository {

    fun notificationStream(): Flow<List<BanterboxNotification>>

    suspend fun refreshNotifications(request: NotificationRequest): Result<PagedData<Int, BanterboxNotification>>
}
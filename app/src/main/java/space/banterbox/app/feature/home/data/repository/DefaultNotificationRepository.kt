package space.banterbox.app.feature.home.data.repository

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import space.banterbox.app.common.util.paging.PagedData
import space.banterbox.app.core.util.NetworkResult
import space.banterbox.app.core.util.NetworkResultParser
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.home.data.source.dto.asDto
import space.banterbox.app.feature.home.data.source.remote.NotificationRemoteDataSource
import space.banterbox.app.feature.home.data.source.remote.model.toNotificationData
import space.banterbox.app.feature.home.domain.model.BanterboxNotification
import space.banterbox.app.feature.home.domain.model.request.NotificationRequest
import space.banterbox.app.feature.home.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.emptyList

class DefaultNotificationRepository @Inject constructor(
    private val remoteDataSource: NotificationRemoteDataSource,
) : NotificationRepository, NetworkResultParser {

    private val notificationsCache = MutableSharedFlow<List<BanterboxNotification>>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        notificationsCache.tryEmit(emptyList())
    }

    override fun notificationStream(): Flow<List<BanterboxNotification>> = notificationsCache

    override suspend fun refreshNotifications(request: NotificationRequest): Result<PagedData<Int, BanterboxNotification>> {
        return when (val networkResult = remoteDataSource.getNotifications(request.asDto())) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    if (networkResult.data.data != null) {
                        val data = networkResult.data.data.toNotificationData()

                        if (request.pagedRequest.key == null) {
                            notificationsCache.tryEmit(data.notifications)
                        } else {
                            val cache = notificationsCache.replayCache.firstOrNull() ?: emptyList()
                            val newNotifications: List<BanterboxNotification> =
                                data.notifications + cache
                            notificationsCache.tryEmit(newNotifications)
                        }
                        Result.Success(
                            PagedData(
                                data = data.notifications,
                                nextKey = if (data.isLastPage) null else data.page + 1,
                                prevKey = null,
                                totalCount = data.totalNotifications,
                            )
                        )
                    } else {
                        emptyResponse(networkResult)
                    }
                } else {
                    badResponse(networkResult)
                }
            }

            else -> parseErrorNetworkResult(networkResult)
        }
    }
}
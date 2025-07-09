package space.banterbox.app.feature.home.domain.model.request

import space.banterbox.app.common.util.paging.PagedRequest

data class NotificationRequest(
    val pagedRequest: PagedRequest<Int>,
)
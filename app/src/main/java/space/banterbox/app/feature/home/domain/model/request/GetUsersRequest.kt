package space.banterbox.app.feature.home.domain.model.request

import space.banterbox.app.common.util.paging.PagedRequest

data class GetUsersRequest(
    val otherUserId: String?,
    val sortBy: String,
    val pagedRequest: PagedRequest<Int>,
)

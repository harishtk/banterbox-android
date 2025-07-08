package space.banterbox.app.feature.home.domain.model.request

import space.banterbox.app.common.util.paging.PagedRequest
import space.banterbox.app.feature.home.domain.model.SearchType

data class SearchRequest(
    val query: String,
    val type: SearchType,
    val pagedRequest: PagedRequest<Int>
)

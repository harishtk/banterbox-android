package space.banterbox.app.feature.home.domain.repository

import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.home.domain.model.SearchResultData
import space.banterbox.app.feature.home.domain.model.request.SearchRequest

interface SearchRepository {

    suspend fun search(request: SearchRequest): Result<SearchResultData>
}
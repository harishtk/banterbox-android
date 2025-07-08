package space.banterbox.app.feature.home.data.repository

import space.banterbox.app.core.util.NetworkResult
import space.banterbox.app.core.util.NetworkResultParser
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.home.data.source.remote.SearchRemoteDataSource
import space.banterbox.app.feature.home.data.source.remote.dto.asDto
import space.banterbox.app.feature.home.data.source.remote.model.toSearchResultData
import space.banterbox.app.feature.home.domain.model.SearchResultData
import space.banterbox.app.feature.home.domain.model.request.SearchRequest
import space.banterbox.app.feature.home.domain.repository.SearchRepository
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

class NetworkOnlySearchRepository @Inject constructor(
    private val remoteDataSource: SearchRemoteDataSource,
) : SearchRepository, NetworkResultParser {

    override suspend fun search(request: SearchRequest): Result<SearchResultData> {
        return when (val networkResult = remoteDataSource.search(request.asDto())) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    Result.Success(networkResult.data.data!!.toSearchResultData())
                } else {
                    badResponse(networkResult)
                }
            }
            else -> parseErrorNetworkResult(networkResult)
        }
    }
}
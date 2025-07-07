package space.banterbox.app.feature.home.data.repository

import space.banterbox.app.common.util.paging.PagedData
import space.banterbox.app.core.di.RepositorySource
import space.banterbox.app.core.di.RepositorySources
import space.banterbox.app.core.net.ApiException
import space.banterbox.app.core.util.NetworkResult
import space.banterbox.app.core.util.NetworkResultParser
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.home.data.source.remote.UserRemoteDataSource
import space.banterbox.app.feature.home.data.source.remote.dto.UserPreviewDto
import space.banterbox.app.feature.home.data.source.remote.dto.toUserPreview
import space.banterbox.app.feature.home.data.source.remote.dto.toUserProfile
import space.banterbox.app.feature.home.data.source.remote.model.GetUsersResponse
import space.banterbox.app.feature.home.data.source.remote.model.UserProfileResponse
import space.banterbox.app.feature.home.domain.model.UserPreview
import space.banterbox.app.feature.home.domain.model.UserProfile
import space.banterbox.app.feature.home.domain.model.request.GetUsersRequest
import space.banterbox.app.feature.home.domain.repository.UserRepository
import space.banterbox.app.feature.home.domain.util.UserFollowUnFollowException
import space.banterbox.app.feature.home.domain.util.UserNotFoundException
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

class NetworkOnlyUserRepository @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource,
) : UserRepository, NetworkResultParser {
    override suspend fun getUser(userId: String): Result<UserProfile> {
        return parseUserProfileResponse(remoteDataSource.getUser(userId))
    }

    override suspend fun getOwnUser(): Result<UserProfile> {
        return parseUserProfileResponse(remoteDataSource.getOwnUser())
    }

    override suspend fun getUsers(request: GetUsersRequest): Result<PagedData<Int, UserPreview>> {
        val page = request.pagedRequest.key ?: 0
        val pageSize = request.pagedRequest.loadSize
        return parseUserPreviewPagedResult(remoteDataSource.getUsers(request.sortBy, page, pageSize));
    }

    override suspend fun followUser(userId: String): Result<UserProfile> {
        return parseUserProfileResponse(remoteDataSource.followUser(userId))
    }

    override suspend fun unfollowUser(userId: String): Result<UserProfile> {
        return parseUserProfileResponse(remoteDataSource.unfollowUser(userId))
    }

    override suspend fun getFollowing(request: GetUsersRequest): Result<PagedData<Int, UserPreview>> {
        val page = request.pagedRequest.key ?: 0
        val pageSize = request.pagedRequest.loadSize
        return parseUserPreviewPagedResult(remoteDataSource.getFollowing(page, pageSize))
    }

    override suspend fun getFollowers(request: GetUsersRequest): Result<PagedData<Int, UserPreview>> {
        val page = request.pagedRequest.key ?: 0
        val pageSize = request.pagedRequest.loadSize
        return parseUserPreviewPagedResult(remoteDataSource.getFollowers(page, pageSize))
    }

    private fun parseUserProfileResponse(networkResult: NetworkResult<UserProfileResponse>): Result<UserProfile> {
        return when (networkResult) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    if (networkResult.data.data != null) {
                        Result.Success(networkResult.data.data.toUserProfile())
                    } else {
                        emptyResponse(networkResult)
                    }
                } else {
                    badResponse(networkResult)
                }
            }

            else -> {
                parseErrorNetworkResult(networkResult)
            }
        }
    }

    private fun parseUserPreviewPagedResult(networkResult: NetworkResult<GetUsersResponse>): Result<PagedData<Int, UserPreview>> {
        return when (networkResult) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    if (networkResult.data.data != null) {
                        val data = networkResult.data.data
                        val nextKey = if (!data.isLastPage) {
                            data.page + 1
                        } else {
                            null
                        }
                        Result.Success(
                            PagedData(
                                data = data.users.map(UserPreviewDto::toUserPreview),
                                nextKey = nextKey,
                                prevKey = null,
                                totalCount = data.total,
                            )
                        )
                    } else {
                        emptyResponse(networkResult)
                    }
                } else {
                    badResponse(networkResult)
                }
            }

            else -> {
                when (networkResult.code) {
                    HttpsURLConnection.HTTP_NOT_FOUND -> {
                        val cause = UserNotFoundException(networkResult.uiMessage ?: "User not found")
                        Result.Error(ApiException(cause))
                    }

                    HttpsURLConnection.HTTP_CONFLICT -> {
                        val cause = UserFollowUnFollowException(
                            networkResult.uiMessage ?: "You are already following this user"
                        )
                        Result.Error(ApiException(cause))
                    }

                    else -> parseErrorNetworkResult(networkResult)
                }
            }
        }
    }
}
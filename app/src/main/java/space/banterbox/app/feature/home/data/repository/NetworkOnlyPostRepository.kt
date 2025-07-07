package space.banterbox.app.feature.home.data.repository

import space.banterbox.app.common.util.paging.PagedRequest
import space.banterbox.app.core.util.NetworkResult
import space.banterbox.app.core.util.NetworkResultParser
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.home.data.source.remote.PostRemoteDataSource
import space.banterbox.app.feature.home.data.source.remote.dto.PostDto
import space.banterbox.app.feature.home.data.source.remote.dto.UserSummaryDto
import space.banterbox.app.feature.home.data.source.remote.dto.toPost
import space.banterbox.app.feature.home.data.source.remote.dto.toUserSummary
import space.banterbox.app.feature.home.domain.model.PostsWithUsers
import space.banterbox.app.feature.home.domain.repository.PostRepository
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

class NetworkOnlyPostRepository @Inject constructor(
    private val remoteDataSource: PostRemoteDataSource,
) : PostRepository, NetworkResultParser {

    override suspend fun globalFeed(request: PagedRequest<Int>): Result<PostsWithUsers> {
        val page = request.key ?: 0
        val pageSize = request.loadSize
        return when (val networkResult = remoteDataSource.getGlobalFeed(page, pageSize)) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    if (networkResult.data.data != null) {
                        val data = networkResult.data.data

                        // Flatten the users from posts
                        val users = data.users
                            .distinctBy { it }
                            .map(UserSummaryDto::toUserSummary)
                        // TODO: --build profile picture url here--

                        val posts = data.posts.map(PostDto::toPost)

                        val nextPagingKey = if (data.page.isLastPage.not()) {
                            data.page.currentPage + 1
                        } else {
                            null
                        }
                        Result.Success(
                            PostsWithUsers(
                                posts = posts,
                                users = users,
                                nextPagingKey = nextPagingKey
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
                parseErrorNetworkResult(networkResult)
            }
        }
    }

    override suspend fun getPrivateFeed(request: PagedRequest<Int>): Result<PostsWithUsers> {
        TODO("Not yet implemented")
    }

    override suspend fun getPostsByAuthorId(
        authorId: String,
        request: PagedRequest<Int>
    ): Result<PostsWithUsers> {
        TODO("Not yet implemented")
    }

    override suspend fun createPost(content: String): Result<PostsWithUsers> {
        TODO("Not yet implemented")
    }

    override suspend fun getPostById(postId: String): Result<PostsWithUsers> {
        TODO("Not yet implemented")
    }

    override suspend fun likePost(postId: String): Result<PostsWithUsers> {
        TODO("Not yet implemented")
    }

    override suspend fun unlikePost(postId: String): Result<PostsWithUsers> {
        TODO("Not yet implemented")
    }
}
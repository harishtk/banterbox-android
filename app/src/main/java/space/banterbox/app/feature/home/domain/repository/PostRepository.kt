package space.banterbox.app.feature.home.domain.repository

import space.banterbox.app.common.util.paging.PagedRequest
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.home.domain.model.PostsWithUsers

interface PostRepository {

    suspend fun globalFeed(request: PagedRequest<Int>): Result<PostsWithUsers>

    suspend fun getPrivateFeed(request: PagedRequest<Int>): Result<PostsWithUsers>

    suspend fun getPostsByAuthorId(authorId: String, request: PagedRequest<Int>): Result<PostsWithUsers>

    suspend fun createPost(content: String): Result<PostsWithUsers>

    suspend fun getPostById(postId: String): Result<PostsWithUsers>

    suspend fun likePost(postId: String): Result<PostsWithUsers>

    suspend fun unlikePost(postId: String): Result<PostsWithUsers>

}
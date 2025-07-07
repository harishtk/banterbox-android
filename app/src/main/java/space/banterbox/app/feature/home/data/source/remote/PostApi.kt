package space.banterbox.app.feature.home.data.source.remote

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import space.banterbox.app.feature.home.data.source.remote.dto.CreatePostRequestDto
import space.banterbox.app.feature.home.data.source.remote.model.PostFeedResponse
import space.banterbox.app.feature.home.data.source.remote.model.SinglePostResponse

interface PostApi {

    @GET("posts/feed/global")
    suspend fun getGlobalFeed(
        @Query("page") page: Int,
        @Query("size") pageSize: Int
    ): Response<PostFeedResponse>

    @GET("posts/feed/private")
    suspend fun getPrivateFeed(
        @Query("page") page: Int,
        @Query("size") pageSize: Int
    ): Response<PostFeedResponse>

    @GET("posts")
    suspend fun getPostsByAuthorId(
        @Query("authorId") authorId: String,
        @Query("page") page: Int,
        @Query("size") pageSize: Int
    ): Response<PostFeedResponse>

    @POST("posts")
    suspend fun createPost(createPostRequestDto: CreatePostRequestDto): Response<SinglePostResponse>

    @GET("posts/{postId}")
    suspend fun getPostById(@Path("postId") postId: String): Response<SinglePostResponse>

    @POST("posts/{postId}/like")
    suspend fun likePost(@Path("postId") postId: String): Response<SinglePostResponse>

    @DELETE("posts/{postId}/like")
    suspend fun unlikePost(@Path("postId") postId: String): Response<SinglePostResponse>
}
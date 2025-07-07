package space.banterbox.app.feature.home.data.source.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import space.banterbox.app.feature.home.data.source.remote.model.GetUsersResponse
import space.banterbox.app.feature.home.data.source.remote.model.UserProfileResponse

interface UserApi {

    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<UserProfileResponse>

    @GET("users/me")
    suspend fun getOwnUser(): Response<UserProfileResponse>

    @GET("users?sort={sortBy}&page={page}&pageSize={pageSize}")
    suspend fun getUsers(
        @Path("sortBy") sortBy: String,
        @Path("page") page: Int,
        @Path("pageSize") pageSize: Int
    ): Response<GetUsersResponse>

    @POST("users/{userId}/follow")
    suspend fun followUser(@Path("userId") userId: String): Response<UserProfileResponse>

    @POST("users/{userId}/unfollow")
    suspend fun unfollowUser(@Path("userId") userId: String): Response<UserProfileResponse>

    @GET("users/following?page={page}&pageSize={pageSize}")
    suspend fun getFollowing(
        @Path("page") page: Int,
        @Path("pageSize") pageSize: Int
    ): Response<GetUsersResponse>

    @GET("users/followers?page={page}&pageSize={pageSize}")
    suspend fun getFollowers(
        @Path("page") page: Int,
        @Path("pageSize") pageSize: Int
    ): Response<GetUsersResponse>

}
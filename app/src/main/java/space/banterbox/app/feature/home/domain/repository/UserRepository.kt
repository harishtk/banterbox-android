package space.banterbox.app.feature.home.domain.repository

import space.banterbox.app.common.util.paging.PagedData
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.home.domain.model.UserPreview
import space.banterbox.app.feature.home.domain.model.UserProfile
import space.banterbox.app.feature.home.domain.model.request.GetUsersRequest

interface UserRepository {

    suspend fun getUser(userId: String): Result<UserProfile>

    suspend fun getOwnUser(): Result<UserProfile>

    suspend fun getUsers(request: GetUsersRequest): Result<PagedData<Int, UserPreview>>

    suspend fun followUser(userId: String): Result<UserProfile>

    suspend fun unfollowUser(userId: String): Result<UserProfile>

    suspend fun getFollowing(request: GetUsersRequest): Result<PagedData<Int, UserPreview>>

    suspend fun getFollowers(request: GetUsersRequest): Result<PagedData<Int, UserPreview>>

}
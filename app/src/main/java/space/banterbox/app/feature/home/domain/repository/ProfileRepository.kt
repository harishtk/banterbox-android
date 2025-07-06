package space.banterbox.app.feature.home.domain.repository

import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.home.domain.model.UserProfile

interface ProfileRepository {

    suspend fun getOwnUserProfile(): Result<UserProfile>

    suspend fun getUserProfile(userId: String): Result<UserProfile>

    suspend fun followUser(userId: String): Result<Unit>

    suspend fun unfollowUser(userId: String): Result<Unit>
}
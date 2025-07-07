package space.banterbox.app.feature.home.data.source.remote.model

import space.banterbox.app.feature.home.data.source.remote.dto.UserProfileDto

data class UserProfileResponse(
    val statusCode: Int,
    val message: String,
    val data: UserProfileDto?
)


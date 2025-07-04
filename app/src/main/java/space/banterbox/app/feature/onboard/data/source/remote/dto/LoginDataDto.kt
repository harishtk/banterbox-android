
package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.core.domain.model.LoginUser
import space.banterbox.app.feature.onboard.domain.model.LoginData

data class LoginDataDto(
    @SerializedName("accessToken")
    val deviceToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("loginUser")
    val loginUserDto: LoginUserDto?,
)

fun LoginDataDto.toLoginData(): LoginData {
    return LoginData(
        loginUser = loginUserDto?.toLoginUser(),
        deviceToken = deviceToken,
        refreshToken = refreshToken,
    )
}

data class LoginUserDto(
    @SerializedName("id")
    val userId: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("displayName")
    val profileName: String,
    @SerializedName("bio")
    val bio: String,
    @SerializedName("profilePictureId")
    val profileImage: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("followersCount")
    val followersCount: Int,
    @SerializedName("followingCount")
    val followingCount: Int,
    @SerializedName("postsCount")
    val postsCount: Int,
)

fun LoginUserDto.toLoginUser(): LoginUser {
    return LoginUser(
        userId = userId,
        username = username,
        profileName = profileName,
        bio = bio,
        profileImage = profileImage,
        createdAt = createdAt,
        followersCount = followersCount,
        followingCount = followingCount,
        postsCount = postsCount
    )
}
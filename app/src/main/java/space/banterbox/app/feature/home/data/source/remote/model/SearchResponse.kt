package space.banterbox.app.feature.home.data.source.remote.model

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.home.data.source.remote.dto.PostSummaryDto
import space.banterbox.app.feature.home.data.source.remote.dto.UserSummaryDto
import space.banterbox.app.feature.home.data.source.remote.dto.toPostSummary
import space.banterbox.app.feature.home.data.source.remote.dto.toUserSummary
import space.banterbox.app.feature.home.domain.model.SearchResultData

data class SearchResponse(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Data?
) {
    data class Data(
        @SerializedName("users")
        val users: List<UserSummaryDto>,
        @SerializedName("posts")
        val posts: List<PostSummaryDto>,
        @SerializedName("usersCount")
        val totalUsers: Int,
        @SerializedName("postsCount")
        val totalPosts: Int
    )
}

fun SearchResponse.Data.toSearchResultData() = SearchResultData(
    users = users.map(UserSummaryDto::toUserSummary),
    posts = posts.map(PostSummaryDto::toPostSummary),
    totalUsers = totalUsers,
    totalPosts = totalPosts
)

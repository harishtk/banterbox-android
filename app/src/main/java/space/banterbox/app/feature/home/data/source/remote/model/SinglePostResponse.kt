package space.banterbox.app.feature.home.data.source.remote.model

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.home.data.source.remote.dto.PostDto
import space.banterbox.app.feature.home.data.source.remote.dto.UserSummaryDto

data class SinglePostResponse(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Data?
) {
    data class Data(
        @SerializedName("post")
        val post: PostDto,
        @SerializedName("users")
        val users: List<UserSummaryDto>
    )
}
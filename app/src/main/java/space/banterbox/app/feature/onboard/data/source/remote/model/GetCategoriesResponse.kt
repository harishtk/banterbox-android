package space.banterbox.app.feature.onboard.data.source.remote.model

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.data.source.remote.dto.CategoriesDataDto

data class GetCategoriesResponse(
    @SerializedName("statusCode")
    val statusCoe: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: CategoriesDataDto?
)


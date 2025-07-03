package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.ShopCategory

data class ShopCategoryDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("category")
    val categoryName: String,
    @SerializedName("createdAt")
    val createdAt: String,
)

fun ShopCategoryDto.toShopCategory(): ShopCategory {
    return ShopCategory(
        id = id, categoryName = categoryName, createdAt = createdAt
    )
}
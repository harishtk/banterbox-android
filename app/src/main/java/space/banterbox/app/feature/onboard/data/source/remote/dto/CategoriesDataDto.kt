package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.feature.onboard.domain.model.CategoriesData

data class CategoriesDataDto(
    @SerializedName("shopCategories")
    val shopCategoriesDto: List<ShopCategoryDto>,
    @SerializedName("productCategories")
    val productCategoriesDto: List<ProductCategoryDto>
)

fun CategoriesDataDto.toCategoriesData(): CategoriesData {
    return CategoriesData(
        shopCategories = shopCategoriesDto.map(ShopCategoryDto::toShopCategory),
        productCategories = productCategoriesDto.map(ProductCategoryDto::toProductCategory)
    )
}

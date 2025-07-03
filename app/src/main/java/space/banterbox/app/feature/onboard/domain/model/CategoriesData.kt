package space.banterbox.app.feature.onboard.domain.model

import space.banterbox.app.core.domain.model.ProductCategory
import space.banterbox.app.feature.onboard.domain.model.ShopCategory

data class CategoriesData(
    val shopCategories: List<ShopCategory>,
    val productCategories: List<ProductCategory>
)
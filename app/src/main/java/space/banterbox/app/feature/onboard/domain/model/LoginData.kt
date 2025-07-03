package space.banterbox.app.feature.onboard.domain.model

import space.banterbox.app.core.domain.model.LoginUser
import space.banterbox.app.core.domain.model.ProductCategory
import space.banterbox.app.core.domain.model.ShopData

data class LoginData(
    val loginUser: LoginUser?,
    val tempId: String,
    val deviceToken: String?,
    val showProfile: Boolean,
    val productCategory: List<ProductCategory>,
    val onboardStep: String,
    val shopData: ShopData?,
) {
    var email: String? = null
    var socialImage: String? = null
}
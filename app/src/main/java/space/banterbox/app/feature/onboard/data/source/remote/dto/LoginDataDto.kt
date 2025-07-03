
package space.banterbox.app.feature.onboard.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import space.banterbox.app.core.domain.model.LoginUser
import space.banterbox.app.core.domain.model.ProductCategory
import space.banterbox.app.core.domain.model.ShopData
import space.banterbox.app.feature.onboard.domain.model.LoginData
import space.banterbox.app.nullAsEmpty

data class LoginDataDto(
    @SerializedName("deviceToken")
    val deviceToken: String,
    @SerializedName("tempId")
    val tempId: String?,
    @SerializedName("loginUser")
    val loginUserDto: LoginUserDto?,
    @SerializedName("profileShow")
    val showProfile: Boolean?,
    @SerializedName("onboardStep")
    val onboardStep: String, /* store, product, bank, launch */
    @SerializedName("productCategories")
    val productCategoriesDto: List<ProductCategoryDto>?,
    @SerializedName("shop")
    val shopDataDto: ShopDataDto?,
    @SerializedName("role")
    val userRole: String?,
)

fun LoginDataDto.toLoginData(): LoginData {
    /* CAUTION: **Login data will not be sent by server so we try to construct it** */
    val loginUser = LoginUser(
        userId = tempId ?: "0",
        username = "",
        profileName = "",
        profileImage = "",
        profileThumb = "",
        notificationCount = 0,
        onboardStep = onboardStep,
        role = userRole.nullAsEmpty(),
    )
    return LoginData(
        loginUser = loginUser,
        deviceToken = deviceToken,
        tempId = tempId ?: "0",
        showProfile = showProfile == true,
        onboardStep = onboardStep,
        productCategory = productCategoriesDto?.map(ProductCategoryDto::toProductCategory) ?: emptyList(),
        shopData = shopDataDto?.toShopData()
    )
}

data class LoginUserDto(
    @SerializedName("userId")
    val userId: String?,
    @SerializedName("username")
    val username: String?,
    @SerializedName("profileName")
    val profileName: String?,
    @SerializedName("profileImage")
    val profileImage: String?,
    @SerializedName("thumbnail")
    val profileThumb: String?,
    @SerializedName("notificationCount")
    val notificationCount: Int,
)

fun LoginUserDto.toLoginUser(tempId: String, onboardStep: String, userRole: String): LoginUser {
    /* Temporarily use the tempId for authentication info. */
    return LoginUser(
        userId = tempId,
        username = username.nullAsEmpty(),
        profileName = profileName.nullAsEmpty(),
        profileImage = profileImage.nullAsEmpty(),
        profileThumb = profileThumb.nullAsEmpty(),
        notificationCount = notificationCount,
        onboardStep = onboardStep,
        role = userRole,
    )
}


data class ProductCategoryDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("createdAt")
    val createdAt: String,
)

fun ProductCategoryDto.toProductCategory(): ProductCategory {
    return ProductCategory(
        id = id,
        categoryName = category,
        createdAt = createdAt
    )
}

data class ShopDataDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("thumbnail")
    val thumbnail: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("image")
    val image: String,
)

fun ShopDataDto.toShopData(): ShopData {
    return ShopData(
        id = id,
        name = name,
        thumbnail = thumbnail,
        category = category,
        description = description,
        address = address,
        image = image
    )
}
package space.banterbox.app.feature.onboard.domain.repository

import space.banterbox.app.core.domain.model.ProductCategory
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.onboard.domain.model.AutoLoginData
import space.banterbox.app.feature.onboard.domain.model.CategoriesData
import space.banterbox.app.feature.onboard.domain.model.DeleteAccountRequest
import space.banterbox.app.feature.onboard.domain.model.LoginData
import space.banterbox.app.feature.onboard.domain.model.ShareLinkData
import space.banterbox.app.feature.onboard.domain.model.ShopCategory
import space.banterbox.app.feature.onboard.domain.model.request.AddBankRequest
import space.banterbox.app.feature.onboard.domain.model.request.AddStoreRequest
import space.banterbox.app.feature.onboard.domain.model.request.AutoLoginRequest
import space.banterbox.app.feature.onboard.domain.model.request.FeedbackRequest
import space.banterbox.app.feature.onboard.domain.model.request.GetShareLinkRequest
import space.banterbox.app.feature.onboard.domain.model.request.LoginRequest
import space.banterbox.app.feature.onboard.domain.model.request.LogoutRequest
import space.banterbox.app.feature.onboard.domain.model.request.SocialLoginRequest
import space.banterbox.app.feature.onboard.domain.model.request.StoreCategoryRequest
import kotlinx.coroutines.flow.Flow

interface AccountsRepository {

    fun loginUser(loginRequest: LoginRequest): Flow<Result<LoginData>>

    fun socialLogin(socialLoginRequest: SocialLoginRequest): Flow<Result<LoginData>>

    fun autoLogin(autoLoginRequest: AutoLoginRequest): Flow<Result<AutoLoginData>>

    fun getShareLink(getShareLinkRequest: GetShareLinkRequest): Flow<Result<ShareLinkData>>

    fun storeCategories(searchQuery: String,): Flow<List<ShopCategory>>

    fun productCategories(searchQuery: String): Flow<List<ProductCategory>>

    suspend fun refreshCategories(storeCategoryRequest: StoreCategoryRequest): Result<CategoriesData>

    suspend fun addStore(addStoreRequest: AddStoreRequest): Result<LoginData>

    suspend fun addBank(addBankRequest: AddBankRequest): Result<String>

    suspend fun launchStore(): Result<String>

    suspend fun feedback(feedbackRequest: FeedbackRequest): Result<String>

    suspend fun logout(logoutRequest: LogoutRequest): Result<String>

    suspend fun deleteAccount(deleteAccountRequest: DeleteAccountRequest): Result<String>
}
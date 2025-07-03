package space.banterbox.app.feature.onboard.data.source.remote

import space.banterbox.app.core.domain.model.BaseResponse
import space.banterbox.app.feature.onboard.data.source.remote.dto.AddBankRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.AddStoreRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.AutoLoginRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.DeleteAccountRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.FeedbackRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.GetShareLinkRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.LaunchStoreRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.LoginRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.LogoutRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.SocialLoginRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.model.AddBankResponse
import space.banterbox.app.feature.onboard.data.source.remote.model.AddStoreResponse
import space.banterbox.app.feature.onboard.data.source.remote.model.AutoLoginResponse
import space.banterbox.app.feature.onboard.data.source.remote.model.GetCategoriesResponse
import space.banterbox.app.feature.onboard.data.source.remote.model.GetShareLinkResponse
import space.banterbox.app.feature.onboard.data.source.remote.model.LaunchStoreResponse
import space.banterbox.app.feature.onboard.data.source.remote.model.LoginResponse
import space.banterbox.app.feature.onboard.domain.model.request.AddStoreRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AccountsApi {

    @POST("user/signup")
    suspend fun login(@Body loginRequestDto: LoginRequestDto): Response<LoginResponse>

    @POST("user/socialLogin")
    suspend fun socialLogin(@Body socialLoginRequestDto: SocialLoginRequestDto): Response<LoginResponse>

    @POST("user/autologin")
    suspend fun autoLogin(@Body autoRequestDto: AutoLoginRequestDto): Response<AutoLoginResponse>

    @POST("user/logout")
    suspend fun logout(@Body logoutRequestDto: LogoutRequestDto): Response<BaseResponse>

    @POST("user/share")
    suspend fun getShareLink(@Body getShareLinkRequestDto: GetShareLinkRequestDto): Response<GetShareLinkResponse>

    @POST("user/categories")
    suspend fun getCategories(): Response<GetCategoriesResponse>

    @POST("user/feedback")
    suspend fun feedback(@Body feedbackRequestDto: FeedbackRequestDto): Response<BaseResponse>

    @POST("user/deleteAccount")
    suspend fun deleteAccount(@Body deleteAccountRequestDto: DeleteAccountRequestDto): Response<BaseResponse>

    @POST("user/addStore")
    suspend fun addStore(@Body addStoreRequestDto: AddStoreRequestDto): Response<AddStoreResponse>

    @POST("user/addBank")
    suspend fun addBank(@Body addBankRequestDto: AddBankRequestDto): Response<AddBankResponse>

    @POST("user/launchStore")
    suspend fun launchStore(@Body launchStoreRequestDto: LaunchStoreRequestDto): Response<LaunchStoreResponse>
}
package space.banterbox.app.feature.onboard.data.source.remote

import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.onboard.data.source.remote.dto.AutoLoginRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.DeleteAccountRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.FeedbackRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.GetShareLinkRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.LogoutRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.SocialLoginRequestDto
import space.banterbox.app.core.data.source.remote.BaseRemoteDataSource
import space.banterbox.app.core.domain.model.BaseResponse
import space.banterbox.app.core.util.NetworkMonitor
import space.banterbox.app.core.util.NetworkResult
import space.banterbox.app.feature.onboard.data.source.remote.dto.AddBankRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.AddStoreRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.LaunchStoreRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.LoginRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.model.AddBankResponse
import space.banterbox.app.feature.onboard.data.source.remote.model.AddStoreResponse
import space.banterbox.app.feature.onboard.data.source.remote.model.AutoLoginResponse
import space.banterbox.app.feature.onboard.data.source.remote.model.GetCategoriesResponse
import space.banterbox.app.feature.onboard.data.source.remote.model.GetShareLinkResponse
import space.banterbox.app.feature.onboard.data.source.remote.model.LaunchStoreResponse
import space.banterbox.app.feature.onboard.data.source.remote.model.LoginResponse
import javax.inject.Inject

/**
 * TODO: Abstract the concrete class to separate implementation
 */
class AccountsRemoteDataSource @Inject constructor(
    netWorkHelper: NetworkMonitor,
    private val apiService: AccountsApi,
) : BaseRemoteDataSource(netWorkHelper) {

    suspend fun login(loginRequestDto: LoginRequestDto): NetworkResult<LoginResponse> =
        safeApiCall { apiService.login(loginRequestDto) }

    suspend fun socialLogin(socialLoginRequestDto: SocialLoginRequestDto): NetworkResult<LoginResponse> =
        safeApiCall { apiService.socialLogin(socialLoginRequestDto) }

    suspend fun autoLogin(autoLoginRequestDto: AutoLoginRequestDto): NetworkResult<AutoLoginResponse> =
        safeApiCall { apiService.autoLogin(autoLoginRequestDto) }

    suspend fun logout(logoutRequestDto: LogoutRequestDto): NetworkResult<BaseResponse> =
        safeApiCall { apiService.logout(logoutRequestDto) }

    suspend fun getShareLink(getShareLinkRequestDto: GetShareLinkRequestDto): NetworkResult<GetShareLinkResponse> =
        safeApiCall { apiService.getShareLink(getShareLinkRequestDto) }

    suspend fun feedback(feedbackRequestDto: FeedbackRequestDto): NetworkResult<BaseResponse> =
        safeApiCall { apiService.feedback(feedbackRequestDto) }

    suspend fun deleteAccount(deleteAccountRequestDto: DeleteAccountRequestDto): NetworkResult<BaseResponse> =
        safeApiCall { apiService.deleteAccount(deleteAccountRequestDto) }

    suspend fun getCategories(): NetworkResult<GetCategoriesResponse> =
        safeApiCall { apiService.getCategories() }

    suspend fun addStore(addStoreRequestDto: AddStoreRequestDto): NetworkResult<AddStoreResponse> =
        safeApiCall { apiService.addStore(addStoreRequestDto) }

    suspend fun addBank(addBankRequestDto: AddBankRequestDto): NetworkResult<AddBankResponse> =
        safeApiCall { apiService.addBank(addBankRequestDto) }

    suspend fun launchStore(launchStoreRequestDto: LaunchStoreRequestDto): NetworkResult<LaunchStoreResponse> =
        safeApiCall { apiService.launchStore(launchStoreRequestDto) }

}
package space.banterbox.app.feature.onboard.data.repository

import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.onboard.data.source.remote.dto.asDto
import space.banterbox.app.feature.onboard.presentation.util.AccountUnavailableException
import space.banterbox.app.feature.onboard.presentation.util.InvalidMobileNumberException
import space.banterbox.app.feature.onboard.presentation.util.OtpLimitReachedException
import space.banterbox.core.common.concurrent.AiaDispatchers
import space.banterbox.core.common.concurrent.Dispatcher
import space.banterbox.app.common.util.InvalidOtpException
import space.banterbox.app.core.di.ApplicationCoroutineScope
import space.banterbox.app.core.domain.model.ProductCategory
import space.banterbox.app.core.net.ApiException
import space.banterbox.app.core.net.HttpResponse
import space.banterbox.app.core.util.NetworkResult
import space.banterbox.app.core.util.NetworkResultParser
import space.banterbox.app.feature.onboard.data.source.remote.AccountsRemoteDataSource
import space.banterbox.app.feature.onboard.data.source.remote.dto.LaunchStoreRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.toAutoLoginData
import space.banterbox.app.feature.onboard.data.source.remote.dto.toCategoriesData
import space.banterbox.app.feature.onboard.data.source.remote.dto.toLoginData
import space.banterbox.app.feature.onboard.data.source.remote.dto.toShareLinkData
import space.banterbox.app.feature.onboard.data.source.remote.model.LoginResponse
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
import space.banterbox.app.feature.onboard.domain.repository.AccountsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

class RemoteOnlyAccountsRepository @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationScope: CoroutineScope,
    private val remoteDataSource: AccountsRemoteDataSource,
    @Dispatcher(AiaDispatchers.Io) private val ioDispatcher: CoroutineDispatcher,
) : AccountsRepository, NetworkResultParser {

    /**
     * A backing hot flow for store categories.
     */
    private val storeCategoriesFlow: MutableSharedFlow<List<ShopCategory>> =
        MutableSharedFlow(
            replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    private val productCategoriesFlow: MutableSharedFlow<List<ProductCategory>> =
        MutableSharedFlow(
            replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    private suspend fun refreshCategoriesInternal(storeCategoryRequest: StoreCategoryRequest): Result<CategoriesData> {
        return when (val networkResult = remoteDataSource.getCategories()) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCoe == HttpsURLConnection.HTTP_OK) {
                    if (networkResult.data.data != null) {
                        val data = networkResult.data.data.toCategoriesData()
                        Result.Success(data)
                    } else {
                        emptyResponse(networkResult)
                    }
                } else {
                    badResponse(networkResult)
                }
            }

            else -> parseErrorNetworkResult(networkResult)
        }
    }

    override fun loginUser(loginRequest: LoginRequest): Flow<Result<LoginData>> = flow {
        emit(remoteDataSource.login(loginRequest.asDto()))
    }
        .map {
            parseLoginResult("", it)
        }
        .flowOn(ioDispatcher)

    override fun socialLogin(socialLoginRequest: SocialLoginRequest): Flow<Result<LoginData>> =
        flow {
            emit(remoteDataSource.socialLogin(socialLoginRequest.asDto()))
        }
            .map {
                parseLoginResult("", it)
            }
            .flowOn(ioDispatcher)

    override fun autoLogin(autoLoginRequest: AutoLoginRequest): Flow<Result<AutoLoginData>> = flow {
        val networkResult = remoteDataSource.autoLogin(autoLoginRequest.asDto())
        emit(
            when (networkResult) {
                is NetworkResult.Success -> {
                    if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                        val data = networkResult.data.data
                        if (data != null) {
                            Result.Success(data.toAutoLoginData())
                        } else {
                            emptyResponse(networkResult)
                        }
                    } else {
                        badResponse(networkResult)
                    }
                }

                else -> parseErrorNetworkResult(networkResult)
            }
        )
    }
        .flowOn(ioDispatcher)

    override suspend fun addStore(addStoreRequest: AddStoreRequest): Result<LoginData> {
        return when (val networkResult = remoteDataSource.addStore(addStoreRequest.asDto())) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    if (networkResult.data.data != null) {
                        val data = networkResult.data.data.toLoginData()
                        Result.Success(data)
                    } else {
                        emptyResponse(networkResult)
                    }
                } else {
                    badResponse(networkResult)
                }
            }

            else -> parseErrorNetworkResult(networkResult)
        }
    }

    override suspend fun addBank(addBankRequest: AddBankRequest): Result<String> {
        return when (val networkResult = remoteDataSource.addBank(addBankRequest.asDto())) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    if (networkResult.data.data != null) {
                        val data = networkResult.data.data.onboardStep
                        Result.Success(data)
                    } else {
                        emptyResponse(networkResult)
                    }
                } else {
                    badResponse(networkResult)
                }
            }

            else -> parseErrorNetworkResult(networkResult)
        }
    }

    override suspend fun launchStore(): Result<String> {
        val dto = LaunchStoreRequestDto(System.currentTimeMillis())
        return when (val networkResult = remoteDataSource.launchStore(dto)) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    if (networkResult.data.data != null) {
                        val data = networkResult.data.data.onboardStep
                        Result.Success(data)
                    } else {
                        emptyResponse(networkResult)
                    }
                } else {
                    badResponse(networkResult)
                }
            }

            else -> parseErrorNetworkResult(networkResult)
        }
    }

    override fun storeCategories(searchQuery: String): Flow<List<ShopCategory>> {
        return storeCategoriesFlow
    }

    override fun productCategories(searchQuery: String): Flow<List<ProductCategory>> {
        return productCategoriesFlow
    }

    override suspend fun refreshCategories(storeCategoryRequest: StoreCategoryRequest): Result<CategoriesData> {
        return when (val refreshResult = refreshCategoriesInternal(storeCategoryRequest)) {
            Result.Loading -> Result.Loading
            is Result.Error -> Result.Error(refreshResult.exception)
            is Result.Success -> {
                refreshResult.data.let { remoteCategoryData ->
                    storeCategoriesFlow.tryEmit(remoteCategoryData.shopCategories)
                    productCategoriesFlow.tryEmit(remoteCategoryData.productCategories)
                }
                refreshResult
            }
        }
    }

    override suspend fun logout(logoutRequest: LogoutRequest): Result<String> {
        return when (val networkResult = remoteDataSource.logout(logoutRequest.asDto())) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    val data = networkResult.data.message ?: "Logout Successful. No message"
                    Result.Success(data)
                } else {
                    badResponse(networkResult)
                }
            }

            else -> parseErrorNetworkResult(networkResult)
        }
    }

    override suspend fun feedback(feedbackRequest: FeedbackRequest): Result<String> {
        return when (val networkResult = remoteDataSource.feedback(feedbackRequest.asDto())) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    val message = networkResult.data?.message
                        ?: "Success. No message."
                    Result.Success(message)
                } else {
                    badResponse(networkResult)
                }
            }

            else -> parseErrorNetworkResult(networkResult)
        }
    }

    override fun getShareLink(getShareLinkRequest: GetShareLinkRequest): Flow<Result<ShareLinkData>> =
        flow {
            val networkResult = remoteDataSource.getShareLink(getShareLinkRequest.asDto())
            emit(
                when (networkResult) {
                    is NetworkResult.Success -> {
                        if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                            val shareLinkDataDto = networkResult.data?.data
                            if (shareLinkDataDto != null) {
                                Result.Success(shareLinkDataDto.toShareLinkData())
                            } else {
                                emptyResponse(networkResult)
                            }
                        } else {
                            badResponse(networkResult)
                        }
                    }

                    else -> parseErrorNetworkResult(networkResult)
                }
            )
        }
            .catch { t ->
                val cause = ApiException(t)
                emit(Result.Error(cause))
            }
            .flowOn(ioDispatcher)

     fun feedbackOld(feedbackRequest: FeedbackRequest): Flow<Result<String>> = flow {
        val networkResult = remoteDataSource.feedback(feedbackRequest.asDto())
        emit(
            when (networkResult) {
                is NetworkResult.Success -> {
                    if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                        val message = networkResult.data?.message
                            ?: "Success. No message."
                        Result.Success(message)
                    } else {
                        badResponse(networkResult)
                    }
                }

                else -> parseErrorNetworkResult(networkResult)
            }
        )
    }
        .flowOn(ioDispatcher)

    override suspend fun deleteAccount(deleteAccountRequest: DeleteAccountRequest): Result<String> {
        return when (val networkResult =
            remoteDataSource.deleteAccount(deleteAccountRequest.asDto())) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    val message = networkResult.data?.message ?: "Success"
                    Result.Success(message)
                } else {
                    badResponse(networkResult)
                }
            }

            else -> {
                when (networkResult.code) {
                    HttpsURLConnection.HTTP_PRECON_FAILED,
                    HttpResponse.HTTP_TOO_MANY_REQUESTS,
                    HttpResponse.HTTP_UNPROCESSABLE_CONTENT,
                    -> {
                        val cause = InvalidMobileNumberException(networkResult.uiMessage)
                        Result.Error(ApiException(cause))
                    }
                    HttpsURLConnection.HTTP_BAD_REQUEST -> {
                        val cause = OtpLimitReachedException()
                        // val cause = RecaptchaException()
                        Result.Error(ApiException(cause))
                    }
                    HttpsURLConnection.HTTP_NOT_ACCEPTABLE -> {
                        val cause = InvalidOtpException(networkResult.message ?: "Invalid OTP!")
                        Result.Error(ApiException(cause))
                    }

                    else -> parseErrorNetworkResult(networkResult)
                }
            }
        }
    }

    private fun parseLoginResult(
        callFor: String,
        networkResult: NetworkResult<LoginResponse>,
    ): Result<LoginData> {
        return when (networkResult) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    val data = networkResult.data.loginDataDto
                    if (callFor == CALL_FOR_VERIFY_OTP) {
                        if (data != null) {
                            Result.Success(data.toLoginData())
                        } else {
                            emptyResponse(networkResult)
                        }
                    } else {
                        Result.Success(
                            LoginData.empty()
                        )
                    }
                } else {
                    badResponse(networkResult)
                }
            }

            else -> {
                Timber.d("Status code check: signup ${networkResult.data?.statusCode}")
                when (networkResult.code) {
                    HttpsURLConnection.HTTP_NOT_ACCEPTABLE -> {
                        val cause = InvalidOtpException(networkResult.message ?: "Invalid OTP!")
                        Result.Error(ApiException(cause))
                    }

                    HttpsURLConnection.HTTP_BAD_REQUEST -> {
                        val cause = OtpLimitReachedException()
                        // val cause = RecaptchaException()
                        Result.Error(ApiException(cause))
                    }

                    HttpsURLConnection.HTTP_GONE -> {
                        val cause = AccountUnavailableException("The account is deleted")
                        Result.Error(ApiException(cause))
                    }

                    HttpsURLConnection.HTTP_PRECON_FAILED,
                    HttpResponse.HTTP_TOO_MANY_REQUESTS,
                    HttpResponse.HTTP_UNPROCESSABLE_CONTENT,
                    -> {
                        val cause = InvalidMobileNumberException(networkResult.uiMessage)
                        Result.Error(ApiException(cause))
                    }

                    else -> {
                        parseErrorNetworkResult(networkResult)
                    }
                }
            }
        }
    }

}

private const val CALL_FOR_VERIFY_OTP = ""
private const val CALL_FOR_SEND_OTP = ""
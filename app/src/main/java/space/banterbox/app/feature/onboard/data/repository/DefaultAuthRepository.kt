package space.banterbox.app.feature.onboard.data.repository

import com.pepul.shops.core.common.concurrent.AiaDispatchers
import com.pepul.shops.core.common.concurrent.Dispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import space.banterbox.app.core.di.ApplicationCoroutineScope
import space.banterbox.app.core.net.ApiException
import space.banterbox.app.core.net.HttpResponse
import space.banterbox.app.core.util.NetworkResult
import space.banterbox.app.core.util.NetworkResultParser
import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.onboard.data.source.remote.AuthRemoteDataSource
import space.banterbox.app.feature.onboard.data.source.remote.dto.asDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.toLoginData
import space.banterbox.app.feature.onboard.domain.model.LoginData
import space.banterbox.app.feature.onboard.domain.model.request.LoginRequest
import space.banterbox.app.feature.onboard.domain.model.request.SignupRequest
import space.banterbox.app.feature.onboard.domain.repository.AuthRepository
import space.banterbox.app.feature.onboard.presentation.util.LoginException
import space.banterbox.app.feature.onboard.presentation.util.UsernameUnavailableException
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

class DefaultAuthRepository @Inject constructor(
    @param:ApplicationCoroutineScope
    private val applicationScope: CoroutineScope,
    private val remoteDataSource: AuthRemoteDataSource,
    @param:Dispatcher(AiaDispatchers.Io) private val ioDispatcher: CoroutineDispatcher,
) : AuthRepository, NetworkResultParser {

    override suspend fun signup(signupRequest: SignupRequest): Result<String> {
        return when (val networkResult = remoteDataSource.signup(signupRequest.asDto())) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_CREATED) {
                    Result.Success(networkResult.data.message)
                } else {
                    badResponse(networkResult)
                }
            }

            else -> {
                when (networkResult.code) {
                    HttpResponse.HTTP_PRECON_REQUIRED -> {
                        val t = UsernameUnavailableException(
                            networkResult.uiMessage ?: "Username is taken"
                        )
                        Result.Error(ApiException(t))
                    }

                    else -> {
                        parseErrorNetworkResult(networkResult)
                    }
                }
            }
        }
    }

    override suspend fun login(loginRequest: LoginRequest): Result<LoginData> {
        return when (val networkResult = remoteDataSource.login(loginRequest.asDto())) {
            is NetworkResult.Success -> {
                if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                    if (networkResult.data.loginDataDto != null) {
                        Result.Success(networkResult.data.loginDataDto.toLoginData())
                    } else {
                        emptyResponse(networkResult)
                    }
                } else {
                    badResponse(networkResult)
                }
            }

            else -> {
                when (networkResult.code) {
                    HttpsURLConnection.HTTP_UNAUTHORIZED -> {
                        val t = LoginException(
                            networkResult.uiMessage ?: "Invalid username or password"
                        )
                        Result.Error(ApiException(t))
                    }

                    else -> parseErrorNetworkResult(networkResult)
                }
            }
        }
    }

}
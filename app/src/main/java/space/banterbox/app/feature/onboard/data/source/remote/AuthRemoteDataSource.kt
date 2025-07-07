package space.banterbox.app.feature.onboard.data.source.remote

import com.google.gson.Gson
import okhttp3.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import space.banterbox.app.BuildConfig
import space.banterbox.app.core.data.source.remote.BaseRemoteDataSource
import space.banterbox.app.core.domain.model.BaseResponse
import space.banterbox.app.core.util.NetworkMonitor
import space.banterbox.app.core.util.NetworkResult
import space.banterbox.app.feature.onboard.data.source.remote.dto.LoginRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.RefreshTokenRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.SignupRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.model.LoginResponse
import space.banterbox.app.feature.onboard.data.source.remote.model.RefreshTokenResponse
import javax.inject.Inject

private const val AUTH_BASE_URL = BuildConfig.API_URL

class AuthRemoteDataSource @Inject constructor(
    networkHelper: NetworkMonitor,
    gson: Gson,
    // We specifically request dagger.Lazy here, so that it's not instantiated from Dagger
    okhttpCallFactory: dagger.Lazy<Call.Factory>,
) : BaseRemoteDataSource(networkHelper) {

    private val apiService = Retrofit.Builder()
        .baseUrl(AUTH_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        // We use callFactory lambda here with dagger.Lazy<Call.Factory>
        // to prevent initializing OkHttp on the main thread.
        .callFactory { okhttpCallFactory.get().newCall(it) }
        .build().create<AuthApi>()

    suspend fun signup(signupRequestDto: SignupRequestDto): NetworkResult<BaseResponse> =
        safeApiCall { apiService.signup(signupRequestDto) }

    suspend fun login(loginRequestDto: LoginRequestDto): NetworkResult<LoginResponse> =
        safeApiCall { apiService.login(loginRequestDto) }

    suspend fun refresh(refreshToken: String): NetworkResult<RefreshTokenResponse> =
        safeApiCall { apiService.refreshToken(refreshToken) }
}
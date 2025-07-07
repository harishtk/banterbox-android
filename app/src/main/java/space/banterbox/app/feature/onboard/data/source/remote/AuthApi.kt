package space.banterbox.app.feature.onboard.data.source.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import space.banterbox.app.core.domain.model.BaseResponse
import space.banterbox.app.feature.onboard.data.source.remote.dto.LoginRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.RefreshTokenRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.dto.SignupRequestDto
import space.banterbox.app.feature.onboard.data.source.remote.model.LoginResponse
import space.banterbox.app.feature.onboard.data.source.remote.model.RefreshTokenResponse

interface AuthApi {

    @POST("auth/signup")
    suspend fun signup(@Body signupRequestDto: SignupRequestDto): Response<BaseResponse>

    @POST("auth/login")
    suspend fun login(@Body loginRequestDto: LoginRequestDto): Response<LoginResponse>

    @GET("auth/refresh")
    suspend fun refreshToken(@Header("Cookie") refreshToken: String): Response<RefreshTokenResponse>
}
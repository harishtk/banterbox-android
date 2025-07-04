package space.banterbox.app.feature.onboard.domain.repository

import space.banterbox.app.core.util.Result
import space.banterbox.app.feature.onboard.domain.model.LoginData
import space.banterbox.app.feature.onboard.domain.model.request.LoginRequest
import space.banterbox.app.feature.onboard.domain.model.request.SignupRequest

interface AuthRepository {

    suspend fun signup(signupRequest: SignupRequest): Result<String>

    suspend fun login(loginRequest: LoginRequest): Result<LoginData>

}
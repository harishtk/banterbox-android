package space.banterbox.app.core.net

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Cookie
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import space.banterbox.app.BuildConfig
import space.banterbox.app.core.persistence.PersistentStore
import space.banterbox.app.core.util.NetworkResult
import space.banterbox.app.feature.onboard.data.source.remote.AuthApi
import space.banterbox.app.feature.onboard.data.source.remote.AuthRemoteDataSource
import space.banterbox.app.feature.onboard.data.source.remote.dto.RefreshTokenRequestDto
import java.io.IOException
import javax.net.ssl.HttpsURLConnection

class TokenAuthenticator(
    private val authREmoteDataSource: AuthRemoteDataSource,
    private val store: PersistentStore,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        synchronized(this) {
            val accessToken = store.deviceToken

            if (response.request.header("Authorization") == null || response.code != HttpsURLConnection.HTTP_UNAUTHORIZED) {
                return null
            }

            // Refresh the token
            val newAccessToken = runBlocking { refreshToken(accessToken) }

            store.setDeviceToken(newAccessToken ?: "")

            // Retry the original request with the new token
            return response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        }
    }

    private suspend fun refreshToken(currentToken: String?): String? {
        return try {
            val request = RefreshTokenRequestDto(store.refreshToken)
//            val tokenCookie = Cookie.Builder()
//                .name("refreshToken")
//                .value(store.refreshToken)
//                .domain(BuildConfig.BASE_URL)
//                .httpOnly()
//                .path("/auth/refresh")
//                .secure()
//                .build()

            val tokenCookie = StringBuilder()
                .append("refreshToken=${store.refreshToken}")
                .append("Path=/auth/refresh")
                .append("Max-Age=604800")
                .append("Secure")
                .append("HttpOnly")
                .toString()

            when (val networkResult = authREmoteDataSource.refresh(tokenCookie)) {
                is NetworkResult.Success -> {
                    return if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                        networkResult.data.data!!.accessToken
                    } else {
                        null
                    }
                }
                is NetworkResult.Error -> {
                    null
                }
            }
        } catch (e: HttpException) {
            null
        } catch (e: IOException) {
            null
        }
    }
}
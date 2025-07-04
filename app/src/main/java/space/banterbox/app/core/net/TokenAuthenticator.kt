package space.banterbox.app.core.net

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import space.banterbox.app.core.persistence.PersistentStore
import space.banterbox.app.feature.onboard.data.source.remote.AuthApi
import space.banterbox.app.feature.onboard.data.source.remote.dto.RefreshTokenRequestDto
import java.io.IOException
import javax.net.ssl.HttpsURLConnection

class TokenAuthenticator(
    private val api: AuthApi,
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
            val response = api.refreshToken(request)
            if (response.isSuccessful) {
                response.body()?.data?.accessToken
            } else {
                null
            }
        } catch (e: HttpException) {
            null
        } catch (e: IOException) {
            null
        }
    }
}
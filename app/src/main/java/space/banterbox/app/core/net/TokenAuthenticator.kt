package space.banterbox.app.core.net

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import space.banterbox.app.core.persistence.PersistentStore
import space.banterbox.app.core.util.NetworkResult
import space.banterbox.app.feature.onboard.data.source.remote.AuthRemoteDataSource
import timber.log.Timber
import java.io.IOException
import javax.net.ssl.HttpsURLConnection


class TokenAuthenticator(
    private val authREmoteDataSource: AuthRemoteDataSource,
    private val store: PersistentStore,
) : Authenticator {
    private val mutex = Mutex()
    private var lastTokenRefreshTime: Long = 0
    private var tokenRefreshTryCount: Int = 0

    companion object {
        private const val TOKEN_REFRESH_DEBOUNCE_MS = 200
        private const val MAX_TOKEN_REFRESH_TRIES = 5
    }

    override fun authenticate(route: Route?, response: Response): Request? = runBlocking {
        mutex.withLock(this) {
            val accessToken = store.deviceToken

            if (response.request.header("Authorization") == null || response.code != HttpsURLConnection.HTTP_UNAUTHORIZED) {
                null
            }

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTokenRefreshTime < TOKEN_REFRESH_DEBOUNCE_MS && tokenRefreshTryCount >= MAX_TOKEN_REFRESH_TRIES) {
                Timber.w("Token refresh debounce: Max tries reached. Permanently unauthorized.")
                null // Permanently unauthorized after max tries within debounce period
            }

            if (currentTime - lastTokenRefreshTime < TOKEN_REFRESH_DEBOUNCE_MS) {
                Timber.d("Token refresh debounce: Waiting before next attempt.")
                // If still within debounce period, but not max tries, retry with existing potentially refreshed token
                // This handles cases where multiple requests fail around the same time
                if (store.deviceToken.isNotEmpty() && store.deviceToken != accessToken) {
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${store.deviceToken}")
                        .build()
                } else {
                    null // Let other interceptors handle or fail if token hasn't changed
                }
            }

            if (tokenRefreshTryCount >= MAX_TOKEN_REFRESH_TRIES) {
                Timber.w("Max token refresh tries reached. Permanently unauthorized.")
                // Clear token to prevent further attempts if desired
                // store.setDeviceToken("")
                // store.setRefreshToken("")
                null
            }

            lastTokenRefreshTime = currentTime
            tokenRefreshTryCount++
            Timber.d("Attempting token refresh, try #$tokenRefreshTryCount")

            // Refresh the token
            val newAccessToken = runBlocking { refreshToken(accessToken) }

            if (newAccessToken != null) {
                store.setDeviceToken(newAccessToken)
                tokenRefreshTryCount = 0 // Reset try count on successful refresh
                Timber.i("Token refreshed successfully.")
                // Retry the original request with the new token
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                Timber.e("Token refresh failed.")
                // If refresh fails and we haven't hit max tries, allow retry which might trigger another auth attempt
                // If max tries hit, this will be caught at the start of the next authenticate call
                null
            }
        }
    }


    private suspend fun refreshToken(currentToken: String?): String? {
        return try {
            val tokenCookie = StringBuilder()
                .append("refreshToken=${store.refreshToken}; ")
                .append("Path=/auth/refresh; ")
                .append("Max-Age=604800; ")
                .append("Secure; ")
                .append("HttpOnly")
                .toString()

            when (val networkResult = authREmoteDataSource.refresh(tokenCookie)) {
                is NetworkResult.Success -> {
                    return if (networkResult.data?.statusCode == HttpsURLConnection.HTTP_OK) {
                        Timber.d("Refresh token API call successful.")
                        networkResult.data.data!!.accessToken
                    } else {
                        Timber.w("Refresh token API call failed with status: ${networkResult.data?.statusCode}")
                        null
                    }
                }

                is NetworkResult.Error -> {
                    Timber.e("Refresh token API call error: ${networkResult.message}, code: ${networkResult.code}")
                    null
                }
            }
        } catch (e: HttpException) {
            Timber.e(e, "HttpException during token refresh")
            null
        } catch (e: IOException) {
            Timber.e(e, "IOException during token refresh")
            null
        }
    }
}
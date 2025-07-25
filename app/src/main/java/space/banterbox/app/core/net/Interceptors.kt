package space.banterbox.app.core.net

import okhttp3.Interceptor
import okhttp3.Response
import space.banterbox.app.eventbus.UnAuthorizedEvent
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import javax.annotation.Nonnegative
import javax.annotation.Nonnull

const val DEFAULT_USER_AGENT = "Android"
const val DEFAULT_PLATFORM = "android"

class AndroidHeaderInterceptor(
    private val versionCode: String,
    private val versionName: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(
            request()
                .newBuilder()
                .addHeader("App-Version-Code", versionCode)
                .addHeader("App-Version-Name", versionName)
                .build()
        )
    }
}

class PlatformInterceptor(
    private val value: String = DEFAULT_PLATFORM,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(
            request()
                .newBuilder()
                .addHeader("Platform", value)
                .build()
        )
    }
}

class GuestUserInterceptor(
    private val provideDeviceToken: () -> String?,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        // Timber.w("NO_GUEST: No guest user flag is configured")
        val isGuestUser = provideDeviceToken().isNullOrBlank()
        proceed(
            request()
                .newBuilder()
                .addHeader("isGuestUser", isGuestUser.toString())
                .build()
        )
    }
}

class UserAgentInterceptor(
    private val value: String = System.getProperty("http.agent") ?: DEFAULT_USER_AGENT,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(
            request()
                .newBuilder()
                .addHeader("User-Agent", value)
                .build()
        )
    }
}

/*class JwtInterceptor(preferencesRepository: AppPreferencesRepository) : Interceptor {
    private val userPreferences = preferencesRepository.userPreferencesFlow

    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        val jwt = runBlocking { userPreferences.first().jwt }
        proceed(
            request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $jwt")
                .build()
        )
    }
}*/

class JwtInterceptor(
    private val provideToken: () -> String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        // Timber.w("NO_JWT: No jwt token is configured")
        val jwt = provideToken()
        proceed(
            request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $jwt")
                .build()
        )
    }
}


/**
 * Catches the [HttpURLConnection.HTTP_FORBIDDEN] and dispatches an [UnAuthorizedEvent] event
 */
class ForbiddenInterceptor(
    private val onForbidden: () -> Unit
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        return proceed(request()).also { response ->
            if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                onForbidden()
            }
        }
    }
}

/**
 * Delays all API calls with the given delay.
 */
class DelayInterceptor : Interceptor {
    interface DelayProvider {
        /**
         * Provides delay duration in milliseconds.
         *
         * @return delay in milliseconds
         */
        @get:Nonnegative
        val delay: Long
    }

    @Nonnull
    private val delayProvider: DelayProvider

    /**
     * Constructs fixed time [DelayInterceptor].
     *
     * @param duration duration of delay
     * @param timeUnit unit of delay
     */
    constructor(@Nonnegative duration: Long, timeUnit: TimeUnit?) {
        require(duration >= 0) { "duration cannot be negative value." }
        if (timeUnit == null) throw NullPointerException("timeUnit cannot be null.")
        delayProvider = object : DelayProvider {
            override val delay: Long
                get() = timeUnit.toMillis(duration)
        }
    }

    /**
     * Constructs [DelayInterceptor] where delay duration is not fixed. For example, using
     * [SimpleDelayProvider] you may change delay during app running by using one of its
     * setters.
     *
     * @param delayProvider delay duration in milliseconds provider. Negative values provided by
     * this provider will cause no delay.
     */
    constructor(delayProvider: DelayProvider?) {
        if (delayProvider == null) throw NullPointerException("delayProvider cannot be null.")
        this.delayProvider = delayProvider
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val delay = delayProvider.delay
        if (delay > 0) {
            try {
                Thread.sleep(delay)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        return chain.proceed(chain.request())
    }
}
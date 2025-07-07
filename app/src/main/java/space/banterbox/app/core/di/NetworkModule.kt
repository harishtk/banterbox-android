package space.banterbox.app.core.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import space.banterbox.app.BuildConfig
import space.banterbox.app.common.util.gson.StringConverter
import space.banterbox.app.core.Env
import space.banterbox.app.core.envForConfig
import space.banterbox.app.core.net.AndroidHeaderInterceptor
import space.banterbox.app.core.net.ForbiddenInterceptor
import space.banterbox.app.core.net.GuestUserInterceptor
import space.banterbox.app.core.net.JwtInterceptor
import space.banterbox.app.core.net.PlatformInterceptor
import space.banterbox.app.core.net.UserAgentInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import space.banterbox.app.core.net.TokenAuthenticator
import space.banterbox.app.core.persistence.PersistentStore
import space.banterbox.app.eventbus.UnAuthorizedEvent
import space.banterbox.app.feature.onboard.data.source.remote.AuthApi
import space.banterbox.app.feature.onboard.data.source.remote.AuthRemoteDataSource
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideOkhttpCallFactory(
        authRemoteDataSource: AuthRemoteDataSource,
        persistentStore: PersistentStore,
    ): okhttp3.Call.Factory {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.MINUTES)

        okHttpClientBuilder.addInterceptor(UserAgentInterceptor())
        okHttpClientBuilder.addInterceptor(
            AndroidHeaderInterceptor(
                versionCode = BuildConfig.VERSION_CODE.toString(),
                versionName = BuildConfig.VERSION_NAME
            )
        )
        okHttpClientBuilder.addInterceptor(
            JwtInterceptor { persistentStore.deviceToken }
        )
        okHttpClientBuilder.addInterceptor(PlatformInterceptor())
        okHttpClientBuilder.addInterceptor(
            GuestUserInterceptor { persistentStore.fcmToken }
        )
        okHttpClientBuilder.authenticator(
            TokenAuthenticator(
                authRemoteDataSource,
                store = persistentStore
            )
        )
        okHttpClientBuilder.addInterceptor(
            ForbiddenInterceptor { EventBus.getDefault().post(UnAuthorizedEvent(System.currentTimeMillis())) }
        )
        // Add delays to all api calls
        // ifDebug { okHttpClientBuilder.addInterceptor(DelayInterceptor(2_000, TimeUnit.MILLISECONDS)) }

        if (envForConfig(BuildConfig.ENV) == Env.DEV || BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            okHttpClientBuilder.addInterceptor(httpLoggingInterceptor)
        }
        return okHttpClientBuilder.build()
    }

    @Singleton
    @Provides
    fun provideOkhttpClient(
        authRemoteDataSource: AuthRemoteDataSource,
        persistentStore: PersistentStore,
    ): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.MINUTES)

        okHttpClientBuilder.addInterceptor(UserAgentInterceptor())
        okHttpClientBuilder.addInterceptor(
            AndroidHeaderInterceptor(
                versionCode = BuildConfig.VERSION_CODE.toString(),
                versionName = BuildConfig.VERSION_NAME
            )
        )
        okHttpClientBuilder.addInterceptor(
            JwtInterceptor { persistentStore.deviceToken }
        )
        okHttpClientBuilder.addInterceptor(PlatformInterceptor())
        okHttpClientBuilder.addInterceptor(
            GuestUserInterceptor { persistentStore.fcmToken }
        )
        okHttpClientBuilder.authenticator(
            TokenAuthenticator(
                authRemoteDataSource,
                store = persistentStore
            )
        )
        okHttpClientBuilder.addInterceptor(
            ForbiddenInterceptor { EventBus.getDefault().post(UnAuthorizedEvent(System.currentTimeMillis())) }
        )

        // Add delays to all api calls
        // ifDebug { okHttpClientBuilder.addInterceptor(DelayInterceptor(2_000, TimeUnit.MILLISECONDS)) }

        if (envForConfig(BuildConfig.ENV) == Env.DEV || BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            okHttpClientBuilder.addInterceptor(httpLoggingInterceptor)
        }
        return okHttpClientBuilder.build()
    }

    @Provides
    @Singleton
    @WebService
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(String::class.java, StringConverter())
        .create()
}
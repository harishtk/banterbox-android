package space.banterbox.app.core.di

import android.content.Context
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.decode.VideoFrameDecoder
import coil.util.DebugLogger
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.Gson
import space.banterbox.app.BuildConfig
import space.banterbox.app.common.util.JsonParser
import space.banterbox.app.common.util.Util
import space.banterbox.app.core.data.repository.DefaultCountryCodeListRepository
import space.banterbox.app.core.data.repository.DefaultUserDataRepository
import space.banterbox.app.core.domain.repository.CountryCodeListRepository
import space.banterbox.app.core.domain.repository.UserDataRepository
import space.banterbox.app.core.persistence.DefaultPersistentStore
import space.banterbox.app.core.persistence.PersistentStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import okhttp3.Call
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /* Coroutine scope */
    @ApplicationCoroutineScope
    @Singleton
    @Provides
    fun provideApplicationScope(): CoroutineScope =
        Util.buildCoroutineScope(
            coroutineName = Util.APPLICATION_COROUTINE_NAME
        )
    /* END - Coroutine scope */

    @GsonParser
    @Provides
    fun provideGsonParser(gson: Gson): JsonParser
            = space.banterbox.app.common.util.GsonParser(gson)

    @Provides
    @Singleton
    fun providePersistentStore(@ApplicationContext application: Context): PersistentStore
            = DefaultPersistentStore.getInstance(application)

    @Provides
    @Singleton
    fun provideReviewManager(@ApplicationContext appContext: Context): ReviewManager =
        ReviewManagerFactory.create(appContext)

    @Provides
    @Singleton
    fun provideAppUpdateManager(@ApplicationContext application: Context): AppUpdateManager =
        AppUpdateManagerFactory.create(application)

    /**
     * Since we're displaying SVGs in the app, Coil needs an ImageLoader which supports this
     * format. During Coil's initialization it will call `applicationContext.newImageLoader()` to
     * obtain an ImageLoader.
     *
     * @see <a href="https://github.com/coil-kt/coil/blob/main/coil-singleton/src/main/java/coil/Coil.kt">Coil</a>
     */
    @Provides
    @Singleton
    fun imageLoader(
        okHttpCallFactory: Call.Factory,
        @ApplicationContext application: Context,
    ): ImageLoader = ImageLoader.Builder(application)
        .callFactory(okHttpCallFactory)
        .components {
            add(SvgDecoder.Factory())
            add(VideoFrameDecoder.Factory())
        }
        // Assume most content images are versioned urls
        // but some problematic images are fetching each time
        .respectCacheHeaders(false)
        .apply {
            if (BuildConfig.DEBUG) {
                logger(DebugLogger())
            }
        }
        .build()
}

@Module
@InstallIn(SingletonComponent::class)
interface AppBinderModule {

    @Binds
    fun bindsUserDataRepository(
        userDataRepository: DefaultUserDataRepository
    ): UserDataRepository

    @Binds
    fun bindsCountryCodeListRepository(
        countryCodeListRepository: DefaultCountryCodeListRepository,
    ): CountryCodeListRepository
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GsonParser

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationCoroutineScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WebService

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class RepositorySource(val repositorySource: RepositorySources)

enum class RepositorySources { Default, RemoteOnly, }
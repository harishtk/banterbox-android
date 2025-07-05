package space.banterbox.app.feature.onboard.domain.di

import dagger.Binds
import space.banterbox.core.common.concurrent.AiaDispatchers
import space.banterbox.core.common.concurrent.Dispatcher
import space.banterbox.app.core.di.ApplicationCoroutineScope
import space.banterbox.app.core.di.WebService
import space.banterbox.app.feature.onboard.data.repository.RemoteOnlyAccountsRepository
import space.banterbox.app.feature.onboard.data.source.remote.AccountsApi
import space.banterbox.app.feature.onboard.data.source.remote.AccountsRemoteDataSource
import space.banterbox.app.feature.onboard.domain.repository.AccountsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import retrofit2.Retrofit
import retrofit2.create
import space.banterbox.app.feature.onboard.data.repository.DefaultAuthRepository
import space.banterbox.app.feature.onboard.data.source.remote.AuthApi
import space.banterbox.app.feature.onboard.domain.repository.AuthRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface OnboardModule {

    @Binds
    @Singleton
    fun bindsAuthRepository(defaultAuthRepository: DefaultAuthRepository): AuthRepository

    companion object {
        @Provides
        @Singleton
        fun provideAccountRepositoryImpl(
            @ApplicationCoroutineScope
            applicationScope: CoroutineScope,
            remoteDataSource: AccountsRemoteDataSource,
            @Dispatcher(AiaDispatchers.Io) ioDispatcher: CoroutineDispatcher
        ): AccountsRepository =
            RemoteOnlyAccountsRepository(
                applicationScope = applicationScope,
                remoteDataSource = remoteDataSource,
                ioDispatcher = ioDispatcher,
            )

        @Provides
        @Singleton
        fun provideAccountsApiService(@WebService retrofit: Retrofit): AccountsApi =
            retrofit.create(AccountsApi::class.java)

        @Provides
        @Singleton
        fun provideAuthApiService(@WebService retrofit: Retrofit): AuthApi =
            retrofit.create<AuthApi>()
    }

}
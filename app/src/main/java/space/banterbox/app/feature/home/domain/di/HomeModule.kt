package space.banterbox.app.feature.home.domain.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.banterbox.app.feature.home.data.repository.DefaultNotificationRepository
import space.banterbox.app.feature.home.data.repository.NetworkOnlyPostRepository
import space.banterbox.app.feature.home.data.repository.NetworkOnlySearchRepository
import space.banterbox.app.feature.home.data.repository.NetworkOnlyUserRepository
import space.banterbox.app.feature.home.domain.repository.NotificationRepository
import space.banterbox.app.feature.home.domain.repository.PostRepository
import space.banterbox.app.feature.home.domain.repository.SearchRepository
import space.banterbox.app.feature.home.domain.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface HomeModule {

    @Binds
    @Singleton
    fun bindUserRepository(
        repository: NetworkOnlyUserRepository
    ): UserRepository

    @Binds
    @Singleton
    fun bindPostRepository(
        repository: NetworkOnlyPostRepository
    ): PostRepository

    @Binds
    @Singleton
    fun bindSearchRepository(
        repository: NetworkOnlySearchRepository
    ): SearchRepository

    @Binds
    @Singleton
    fun bindNotificationRepository(
        repository: DefaultNotificationRepository
    ): NotificationRepository
}
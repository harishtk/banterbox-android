package space.banterbox.app.feature.home.domain.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.banterbox.app.core.di.RepositorySource
import space.banterbox.app.core.di.RepositorySources
import space.banterbox.app.feature.home.data.repository.NetworkOnlyUserRepository
import space.banterbox.app.feature.home.domain.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface HomeModule {

    @Binds
    @Singleton
    fun bindUserRepository(
        repository: NetworkOnlyUserRepository)
    : UserRepository
}
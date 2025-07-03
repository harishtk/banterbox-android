package space.banterbox.app.core.di

import android.app.Application
import space.banterbox.app.core.di.AppDependencies
import space.banterbox.app.core.persistence.DefaultPersistentStore
import space.banterbox.app.core.persistence.PersistentStore
import space.banterbox.app.core.util.AppForegroundObserver

class AppDependenciesProvider(private val application: Application) : AppDependencies.Provider {
    override fun provideAppForegroundObserver(): AppForegroundObserver {
        return AppForegroundObserver()
    }

    override fun providePersistentStore(): PersistentStore {
        return DefaultPersistentStore.getInstance(application)
    }


}
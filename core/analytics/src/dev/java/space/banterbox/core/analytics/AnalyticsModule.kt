package space.banterbox.core.analytics

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AnalyticsModule {
    @Binds
    fun bindsAnalyticsLogger(
        stubAnalyticsLogger: StubAnalyticsLogger
    ) : AnalyticsLogger
}
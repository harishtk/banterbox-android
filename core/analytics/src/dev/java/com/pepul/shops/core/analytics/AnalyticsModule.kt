package com.pepul.shops.core.analytics

import com.pepul.shops.core.analytics.AnalyticsLogger
import com.pepul.shops.core.analytics.StubAnalyticsLogger
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
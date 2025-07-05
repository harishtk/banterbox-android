package com.aiavatar.core.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import space.banterbox.core.analytics.AnalyticsLogger
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    abstract fun bindAnalyticsLogger(
        firebaseAnalyticsLogger: FirebaseAnalyticsLogger
    ): AnalyticsLogger

    companion object {
        @Provides
        @Singleton
        fun providesFirebaseAnalytics(): FirebaseAnalytics { return Firebase.analytics }
    }
}
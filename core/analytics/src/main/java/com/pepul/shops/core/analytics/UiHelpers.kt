package com.pepul.shops.core.analytics

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Global key used to obtain access to the [AnalyticsLogger] through a CompositionLocal.
 */
val LocalAnalyticsLogger = staticCompositionLocalOf<AnalyticsLogger> {
    // Provide a default AnalyticsLogger which does nothing. This is so that tests and previews
    // do not have to provide one. For real app builds provide a different implementation.
    NoopAnalyticsLogger()
}
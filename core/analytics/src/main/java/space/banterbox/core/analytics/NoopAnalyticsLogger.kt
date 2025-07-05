package space.banterbox.core.analytics

import android.os.Bundle

@Suppress("unused")
class NoopAnalyticsLogger : AnalyticsLogger {
    override fun logEvent(event: AnalyticsEvent) = Unit
    override fun logEvent(name: String, params: Bundle?) = Unit
    override fun logEvent(name: String) = Unit
    override fun setUserId(userId: String?) = Unit
}
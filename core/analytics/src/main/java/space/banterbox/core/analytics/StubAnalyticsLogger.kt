package space.banterbox.core.analytics

import android.os.Bundle
import android.util.Log
import javax.inject.Inject

private const val Tag = "StubAnalyticsLogger"

/**
 * A [AnalyticsLogger] to print the events in the logcat.
 */
class StubAnalyticsLogger @Inject constructor() : AnalyticsLogger {
    override fun logEvent(event: AnalyticsEvent) {
        Log.d(Tag, "${event.type} [${event.params}]")
    }

    override fun logEvent(name: String, params: Bundle?) {
        Log.d(Tag, "$name [${params.toString()}]")
    }

    override fun logEvent(name: String) {
        Log.d(Tag, "$name")
    }

    override fun setUserId(userId: String?) {
        Log.d(Tag, "user id = $userId")
    }
}
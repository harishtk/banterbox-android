package com.aiavatar.core.analytics

import android.os.Bundle
import androidx.annotation.Size
import com.google.firebase.analytics.FirebaseAnalytics
import com.pepul.shops.core.analytics.AnalyticsEvent
import com.pepul.shops.core.analytics.AnalyticsLogger
import javax.inject.Inject

private fun List<AnalyticsEvent.Param>.asBundle(): Bundle? {
    return if (isEmpty()) { null }
    else {
        Bundle().apply {
            forEach { param ->
                putString(param.key, param.value)
            }
        }
    }
}

class FirebaseAnalyticsLogger @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    // private val facebookAnalytics: AppEventsLogger
) : AnalyticsLogger {


    override fun logEvent(event: AnalyticsEvent) {
        logEvent(event.type, event.params.asBundle())
    }

    override fun logEvent(@Size(min = 1L,max = 40L) name: String, params: Bundle?) {
        firebaseAnalytics.logEvent(name, params)

        /*val adjustToken = ACTIVE_ADJUST_EVENT_TOKEN_MAP[name]
        adjustToken?.let {
            val adjustEvent = AdjustEvent(adjustToken)
            Adjust.trackEvent(adjustEvent)
        }*/
    }

    override fun logEvent(name: String) {
        logEvent(name = name, params = null)
    }

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }
}
package space.banterbox.core.analytics

import android.os.Bundle
import androidx.annotation.Size

interface AnalyticsLogger {

    fun logEvent(event: AnalyticsEvent)

    fun logEvent(@Size(min = 1L, max = 40L) name: String, params: Bundle?)

    fun logEvent(@Size(min = 1L, max = 40L) name: String)

    fun setUserId(userId: String?)
}


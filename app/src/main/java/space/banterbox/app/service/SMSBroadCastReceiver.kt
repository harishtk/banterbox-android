package space.banterbox.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import space.banterbox.app.parcelable

class SMSBroadCastReceiver : BroadcastReceiver() {

    var smsRetrieverListener: OnSmsRetrieveListener? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.apply {
            action?.let { action ->
                if (action == SmsRetriever.SMS_RETRIEVED_ACTION) {
                    extras?.get(SmsRetriever.EXTRA_STATUS)?.also { value ->
                        val smsRetrieverStatus = value as Status
                        if (smsRetrieverStatus.statusCode == CommonStatusCodes.SUCCESS) {
                            val messageIntent =
                                extras?.parcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                            smsRetrieverListener?.onRetrieveSms(SMS_RECEIVER_SUCCESS, messageIntent)
                        } else if (smsRetrieverStatus.statusCode == CommonStatusCodes.TIMEOUT) {
                            smsRetrieverListener?.onRetrieveSms(SMS_RECEIVER_TIME_OUT, null)
                        }
                    }
                }
            }
        }
    }

    interface OnSmsRetrieveListener {
        fun onRetrieveSms(status: String, intent: Intent?)
    }

    companion object {
        /** Sms receiver **/
        const val SMS_RECEIVER_SUCCESS = "SMS_RECEIVER_SUCCESS"
        const val SMS_RECEIVER_TIME_OUT = "SMS_RECEIVER_TIME_OUT"
    }
}

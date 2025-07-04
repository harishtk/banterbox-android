package space.banterbox.app.core.persistence

interface PersistentStore {
    val deviceId: String
    val deviceToken: String
    val refreshToken: String
    val tempId: String
    val fcmToken: String
    val isFcmTokenSynced: Boolean
    val lastFcmTokenSyncTime: Long
    val deepLinkKey: Pair<String, String>
    val installReferrerFetched: Boolean

    fun getOrCreateDeviceId(): String

    fun setDeviceToken(token: String): String
    fun setRefreshToken(token: String): String

    fun setTempId(tempId: String)

    fun setFcmToken(token: String)
    fun setFcmTokenSynced(isSynced: Boolean): Boolean
    fun setLastTokenSyncTime(timeMillis: Long): Boolean
    fun setDeepLinkKey(keyType: String, key: String)
    fun setInstallReferrerFetched(fetched: Boolean)
    fun logout()
}
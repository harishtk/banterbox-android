package space.banterbox.app

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.i18n.phonenumbers.PhoneNumberUtil
import space.banterbox.app.common.util.Inspector
import space.banterbox.app.common.util.AppStartup
import space.banterbox.app.common.util.ByteUnit
import space.banterbox.app.common.util.logging.timber.NoopTree
import space.banterbox.app.core.Env
import space.banterbox.app.core.di.ApplicationCoroutineScope
import space.banterbox.app.core.di.AppDependencies
import space.banterbox.app.core.di.AppDependenciesProvider
import space.banterbox.app.core.envForConfig
import space.banterbox.app.core.net.NetworkSpeedMonitor
import space.banterbox.app.core.util.AppForegroundObserver
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import javax.inject.Inject
import javax.inject.Provider
import kotlin.system.exitProcess

/**
 * [Application] class for Banterbox Space
 */
@HiltAndroidApp
class ApplicationContext : Application(),  AppForegroundObserver.Listener, ImageLoaderFactory {
    @Inject
    lateinit var imageLoader: Provider<ImageLoader>

    @Inject @ApplicationCoroutineScope
    lateinit var applicationScope: CoroutineScope

    private val networkMonitor: NetworkSpeedMonitor by lazy {
        NetworkSpeedMonitor.getInstance(3_000L)
    }

    private var installReferrerClient: InstallReferrerClient? = null

    override fun onCreate() {
        AppStartup.getInstance().onApplicationCreate()
        val startTime = System.currentTimeMillis()
        super.onCreate()

        val uid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(BuildConfig.APPLICATION_ID, PackageManager.ApplicationInfoFlags.of(
                PackageManager.GET_META_DATA.toLong())).uid
        } else {
            @Suppress("deprecation")
            packageManager.getApplicationInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_META_DATA).uid
        }

        AppStartup.getInstance()
            .addBlocking("init-logging", this::initializeLogging)
            .addBlocking("app-dependencies", this::initApplicationDependencies)
            .addBlocking("lifecycle-observer") {
                AppDependencies.appForegroundObserver?.addListener(this)
            }
            // .addBlocking("after-create", this::setupApp)
            .addNonBlocking(this::checkEmulator)
            .addNonBlocking(this::checkInstallReferrer)
            .addPostRender(this::setupApp)
            .addPostRender {
                Timber.tag("NetworkSpdMonitor").d("uid=$uid")
                networkMonitor.setAppUid(uid)
            }
            .execute()

        Log.d(
            Tag,
            "onCreate() took " + (System.currentTimeMillis() - startTime) + " ms"
        )

        applicationScope.launch {
            networkMonitor.monitor().collectLatest { speed ->
                val kbps = ByteUnit.BYTES.toKilobytes(speed.bytesPerSecond)
                Timber.tag("NetworkSpdMonitor").d("$kbps KBps")
            }
        }
    }

    private fun setupApp() {
        // AppDependencies.persistentStore?.getOrCreateDeviceId()
        if (envForConfig(BuildConfig.ENV) != Env.PROD) {
            Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException)
        }
    }

    private fun initApplicationDependencies() {
        AppDependencies.init(
            application = this,
            provider = AppDependenciesProvider(this)
        )
        PhoneNumberUtil.init(this)
    }

    private fun initializeLogging() {
        when (envForConfig(BuildConfig.ENV)) {
            Env.DEV -> {
                Timber.plant(Timber.DebugTree())
            }
            else -> {
                if (BuildConfig.DEBUG) {
                    Timber.plant(Timber.DebugTree())
                } else {
                    Timber.plant(NoopTree())
                }
            }
        }
    }

    /**
     * Checks if the install referrer is available. if yes, store the details in the preferences.
     */
    private fun checkInstallReferrer() {
        val prefs = AppDependencies.persistentStore!!
        if (!prefs.installReferrerFetched) {
            Timber.tag("Referral.Msg").i("InstallReferralService init")
            // appLogger.log("Referral.Msg", "InstallReferralService init")
            installReferrerClient = InstallReferrerClient.newBuilder(this).build().apply {
                startConnection(object : InstallReferrerStateListener {
                    override fun onInstallReferrerSetupFinished(p0: Int) {
                        Timber.tag("Referral.Msg").i("InstallReferralService setup finished.")
                        // appLogger.log("Referral.Msg", "InstallReferralService init")
                        when (p0) {
                            InstallReferrerClient.InstallReferrerResponse.OK -> {
                                Timber.tag("Referral.Msg").i("Install Referrer. OK")
                                // appLogger.log("Referral.Msg", "Install Referrer. OK")
                                val response = installReferrerClient?.installReferrer ?: return
                                Timber.tag("Referral.Msg").i("Install Referral URL: ${response.installReferrer} ${response.installVersion}")
                                // appLogger.log("Referral.Msg", "Install Referral URL: ${response.installReferrer}")

                                // Parse sample:  utm_campaign=GTVm5tFUsXvj&utm_medium=invite&utm_source=pepulnow
                                val utmParameters = parseUtmParameters(response.installReferrer)
                                if (utmParameters["utm_source"] == "google") {
                                    if (utmParameters["utm_campaign"] != null) {
                                        // key
                                        val keytype = utmParameters["utm_medium"] ?: ""
                                        val key = utmParameters["utm_campaign"] ?: ""
                                        if (keytype.isNotBlank() && key.isNotBlank()) {
                                            prefs.setDeepLinkKey(keytype, key)
                                            prefs.setInstallReferrerFetched(true)
                                            installReferrerClient?.endConnection()
                                        }
                                    }
                                }
                            }
                            InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                                Timber.tag("Referral.Msg")
                                    .i("Install Referrer not supported for current app in Play Store.")
                                // appLogger.log("Referral.Msg", "Install Referrer not supported for current app in Play Store.")
                            }
                            InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                                Timber.tag("Referral.Msg")
                                    .i("Install Referrer service unavailable. Failed to establish connection.")
                                // appLogger.log("Referral.Msg", "Install Referrer service unavailable. Failed to establish connection.")
                            }
                        }
                    }

                    override fun onInstallReferrerServiceDisconnected() {
                        Timber.tag("Referral.Msg").w("InstallReferralService disconnected.")
                        // appLogger.log("Referral.Msg", "InstallReferralService disconnected.")
                    }
                })
            }

        } else {
            Timber.tag("Referral.Msg").d("Install Referrer already fetched.")
            // appLogger.log("Referral.Msg", "Install Referrer already fetched.")
        }
    }

    private fun checkEmulator() {
        if (!BuildConfig.DEBUG) {
            envForConfig(BuildConfig.ENV).let { env ->
                if (env == Env.PROD || env == Env.SPECIAL) {
                    if (Inspector.checkEmulatorFiles()) {
                        Log.e(Tag, "Runtime error", RuntimeException("Invalid runtime. Emulator."))
                        exitProcess(-1)
                    }
                }
            }
        }
    }

    private fun handleUncaughtException(thread: Thread, e: Throwable) {
        Log.e("UncaughtException", "The exception was unhandled", e)
        val result: Writer = StringWriter()
        val printWriter = PrintWriter(result)
        e.printStackTrace(printWriter)
        printWriter.close()
        var arr = e.stackTrace
        val report = StringBuilder(
            """$e""".trimIndent()
        )
        report.append("--------- Stack trace ---------\n\n")
        for (stackTraceElement in arr) {
            report.append("    ").append(stackTraceElement.toString()).append("\n")
        }
        report.append("-------------------------------\n\n")
        val cause: Throwable? = e.cause
        if (cause != null) {
            report.append("--------- Cause ---------\n\n")
            report.append(cause.toString()).append("\n\n")
            arr = cause.stackTrace
            for (stackTraceElement in arr) {
                report.append("    ").append(stackTraceElement.toString()).append("\n")
            }
            report.append("-------------------------------\n\n")
        }
        sendEmail(report.toString())
    }

    private fun sendEmail(crash: String) {
        try {
            val reportContent = """
            |DEVICE OS VERSION CODE: ${Build.VERSION.SDK_INT}
            |DEVICE VERSION CODE NAME: ${Build.VERSION.CODENAME}
            |DEVICE NAME: ${Build.MANUFACTURER} ${Build.MODEL}
            |VERSION CODE: ${BuildConfig.VERSION_CODE}
            |VERSION NAME: ${BuildConfig.VERSION_NAME}
            |PACKAGE NAME: ${BuildConfig.APPLICATION_ID}
            |BUILD TYPE: ${BuildConfig.BUILD_TYPE}

            |$crash
            """.trimIndent().trimMargin()
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            emailIntent.data = Uri.parse("mailto:") // only email apps should handle this
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(CRASH_REPORT_EMAIL))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Crash Report")
            emailIntent.putExtra(Intent.EXTRA_TEXT, reportContent)

            try {
                //start email intent
                val chooser = Intent.createChooser(emailIntent, "Email")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (chooser.resolveActivity(packageManager) != null) {
                    startActivity(chooser)
                    System.exit(1)
                } else {
                    throw IllegalStateException("Cannot perform this action!")
                }
            } catch (e: Exception) {
                //if any thing goes wrong for example no email client application or any exception
                //get and show exception message
                e.printStackTrace()
            }
        } catch (e: Exception) {
            //Timber.tag(TAG).e(TAG, "sendEmail: %s", e.message)
        }
    }

    override fun onForeground() {
        super.onForeground()
        networkMonitor.start()
    }

    override fun onBackground() {
        super.onBackground()
        networkMonitor.stop()
    }

    override fun newImageLoader(): ImageLoader = imageLoader.get()

    companion object {
        const val Tag = "Banterbox.Space"

        const val CRASH_REPORT_EMAIL = "support@banterbox.space"

        @Volatile
        var currentVisibleScreen: String = ""
            private set

        @JvmName("setCurrentVisibleScreen1")
        @Synchronized
        fun setCurrentVisibleScreen(tag: String) {
            synchronized(this) {
                currentVisibleScreen = tag
            }
        }

        @Synchronized
        fun clearCurrentVisibleScreen() {
            synchronized(this) {
                currentVisibleScreen = ""
            }
        }
    }
}
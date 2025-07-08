@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import kotlin.collections.setOf
import kotlin.collections.listOf
import kotlin.arrayOf

plugins {
    alias(libs.plugins.android.application)
    // alias(libs.plugins.firebase.crashlytics)
    // alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.nav.safeargs)
}

object Ext {
    const val versionMajor = 0 // Major
    const val versionMinor = 0 // Minor
    const val versionPatch = 1 // Patches, updates
    val versionClassifier: String? = null
    const val versionRevision = "revision-01"
    const val prodRevision = "rc-01"
    const val isSnapshot = false
    const val minSdk = 26
    const val targetSdk = 34
}

android {
    namespace = "space.banterbox.app"
    compileSdk = libs.versions.compileSdk.get().toIntOrNull()

    signingConfigs {
        val localProps = Properties()
        if (rootProject.file("local.properties").exists()) {
            localProps.load(rootProject.file("local.properties").inputStream())
        }
        create("release") {
//            storeFile = file(localProps["RELEASE_STORE_FILE"] as String)
//            storePassword = localProps["RELEASE_STORE_PASSWORD"] as String
//            keyAlias = localProps["RELEASE_KEY_ALIAS"] as String
//            keyPassword = localProps["RELEASE_KEY_PASSWORD"] as String

            /*enableV3Signing = true
            enableV4Signing = true*/
        }
    }

    defaultConfig {
        applicationId = "space.banterbox.app"
        minSdk = Ext.minSdk
        targetSdk = Ext.targetSdk
        versionCode = generateVersionCode()
        versionName = generateVersionName()
        androidResources.localeFilters += setOf("en")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    project.ext.set("googleServicesResourceRoot", "/")

    buildTypes {
        getByName("release") {
            isShrinkResources = false
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }

        create("benchmark") {
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf<String>("release")
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
            freeCompilerArgs =
                listOf(
                    "-opt-in=kotlin.RequiresOptIn",
                    // Enable experimental coroutines APIs, including Flow
                    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-opt-in=kotlinx.coroutines.FlowPreview",
                    "-opt-in=kotlin.Experimental",
                    "-Xjvm-default=all-compatibility"
                )
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get() as String
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets {
        getByName("main") {
            // The libs directory contains prebuilt libraries that are used by the
            // app's library defined in CMakeLists.txt via an IMPORTED target.
            jniLibs.srcDirs("libs")
        }
        create("dev") {
            java {
                srcDirs("src/dev/java")
            }
        }
        create("internal") {
            java {
                srcDirs("src/internal/java", "src/prod/java")
            }
        }
        create("prod") {
            java {
                srcDirs("src/prod/java")
            }
        }
    }

    flavorDimensions.add("default")
    productFlavors {
        create("dev") {
            dimension = "default"

            buildConfigField("String", "BASE_URL", "\"http://192.168.1.49:8000\"")
            buildConfigField("String", "API_URL", "\"http://192.168.1.49:8000/api/v1/\"")
            buildConfigField("String", "THUMBNAIL_BASE_URL",
                "\"https://d1whtbopipnjq0.cloudfront.net/thumbnail/\""
            )
            buildConfigField("String", "SOCKET_URL", "\"http://43.205.53.52\"")
            buildConfigField("String", "ENV", "\"dev\"")
            buildConfigField("boolean", "IS_SECURED", "false")
            buildConfigField("String", "S3_BUCKET", "\"https://d1whtbopipnjq0.cloudfront.net/\"")
            versionNameSuffix = "-dev${Ext.versionRevision}"
        }

        create("internal") {
            dimension = "default"

            buildConfigField("String", "BASE_URL", "\"https://pepzoondev.hifrds.com\"")
            buildConfigField("String", "API_URL", "\"https://pepzoondev.hifrds.com/api/v4/\"")
            buildConfigField("String", "THUMBNAIL_BASE_URL",
                "\"https://d1whtbopipnjq0.cloudfront.net/thumbnail/\""
            )
            buildConfigField("String", "SOCKET_URL", "\"http://43.205.53.52/\"")
            buildConfigField("String", "ENV", "\"internal\"")
            buildConfigField("boolean", "IS_SECURED", "false")
            buildConfigField("String", "S3_BUCKET", "\"https://d1whtbopipnjq0.cloudfront.net/\"")
            versionNameSuffix = "-internal"
        }

        create("prod") {
            dimension = "default"

            buildConfigField("String", "BASE_URL", "\"https://shops.storesnearme.in\"")
            buildConfigField("String", "API_URL", "\"https://shops.storesnearme.in/api/v3/\"")
            buildConfigField("String", "THUMBNAIL_BASE_URL",
                "\"https://d1whtbopipnjq0.cloudfront.net/liveThumb/\""
            )
            buildConfigField("String", "SOCKET_URL", "\"http://43.205.53.52/\"")
            buildConfigField("String", "ENV", "\"prod\"")
            buildConfigField("boolean", "IS_SECURED", "true")
            buildConfigField("String", "S3_BUCKET", "\"https://d1whtbopipnjq0.cloudfront.net/\"")
        }
    }
}

dependencies {

    implementation(
        fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar")))
    )

    implementation(project(":core:analytics"))
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.asynclayoutinflater)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.metrics.performance)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.security.crypto.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.window)
    implementation(libs.android.installreferrer)
    implementation(libs.bundles.camerax)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.profileinstaller)

    /* Coil */
    implementation(libs.coil.kt)
    implementation(libs.coil.kt.compose)
    implementation(libs.coil.kt.svg)
    implementation(libs.coil.kt.video)

    /* Compose */
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.compose.accompainst.themeadaptermaterial)
    implementation(libs.compose.material3)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.compose.animation)
    api(libs.androidx.compose.material.iconsExtended)

    /* Google */
    implementation(libs.google.material)
//    implementation(libs.google.play.core.ktx)
//    implementation(libs.google.play.services.auth)
    implementation(libs.google.play.review.ktx)
    implementation(libs.google.play.appupdate.ktx)
    implementation(libs.google.android.flexbox)
    /* For Sms reader */
//    implementation(libs.google.play.services.auth.api.phone)

    /* Lib PhoneNumber */
    implementation(libs.lionscribe.libphonenumber)

    /* Firebase */
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth) {
        exclude(module = "play-services-safetynet")
    }
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.dynamiclinks)
    implementation(libs.firebase.crashlytics)

    /* Kotlinx Coroutines */
    implementation(libs.kotlinx.coroutines.android)
    /* Kotlinx Serialization */
    implementation(libs.kotlinx.serialization.json)

    /* Hilt */
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation)
    ksp(libs.hilt.android.compiler)

    // Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // Paging 3
    implementation(libs.androidx.paging)

    // Work
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    /* Exoplayer */
    implementation(libs.bundles.exoplayer.all)

    /* Timber */
    implementation(libs.timber)

    /* EventBus */
    implementation(libs.eventbus)

    /* Lottie Animation */
    implementation(libs.lottie.compose)

    /* OTP View */
    implementation(libs.mukeshsolanki.otpview)

    /* Retrofit */
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.okhttp3.logging.interceptor)

    /* Navigation Components */
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.navigation.ui.ktx)
    implementation(libs.androidx.hilt.navigation)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose.ktx)

    androidTestImplementation(libs.androidx.navigation.testing)

    /* Lifecycle */
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.common)
    implementation(libs.androidx.lifecycle.lifecycle.process)
    implementation(libs.androidx.lifecycle.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.extensions)

    /* Glide */
    implementation(libs.glide)
    implementation(libs.glide.transformations)
    implementation(libs.glide.okhttp3.integration) {
        exclude(group = "glide-parent")
    }
    ksp(libs.glide.compiler)

    implementation(libs.facebook.shimmer)

    /* View Pager Indicator */
    implementation(libs.zhpanvip.viewpagerindicator)

    // Core library
    testImplementation(libs.junit)
    debugImplementation(testLibs.androidx.testing)
    debugImplementation(testLibs.androidx.arch.testing)
    debugImplementation(testLibs.androidx.fragment.testing)

    // AndroidJUnitRunner and JUnit Rules
    androidTestImplementation(testLibs.androidx.test.runner)
    androidTestImplementation(testLibs.androidx.test.rules)

    // Assertions
    androidTestImplementation(testLibs.androidx.test.junitext)
    androidTestImplementation(testLibs.androidx.test.truthext)
    testImplementation(testLibs.googletruth)

    // Espresso dependencies
    androidTestImplementation(testLibs.androidx.test.espressoCore)
    androidTestImplementation(testLibs.androidx.test.espressoContrib)
    androidTestImplementation(testLibs.androidx.test.espressoIntents)
    androidTestImplementation(testLibs.androidx.test.espressoAccessibility)
    androidTestImplementation(testLibs.androidx.test.espressoWeb)
    androidTestImplementation(testLibs.androidx.test.espressoConcurrent)
    androidTestImplementation(testLibs.androidx.test.espressoIdlingResource)

    // Hilt testing
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)

    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)

    // Coroutines testing
    testImplementation(libs.kotlinx.coroutines.test)

    /* LeakCanary to detect memory leaks */
    // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.10")
}

@SuppressWarnings("GrMethodMayBeStatic")
fun generateVersionCode(): Int {
    return Ext.minSdk * 10000000 + Ext.versionMajor * 10000 + Ext.versionMinor * 100 + Ext.versionPatch
}

@SuppressWarnings("GrMethodMayBeStatic")
fun generateVersionName(): String {
    var versionName: String = "${Ext.versionMajor}.${Ext.versionMinor}.${Ext.versionPatch}"
    val classifier = when {
        Ext.versionClassifier != null -> Ext.versionClassifier
        Ext.isSnapshot -> Ext.prodRevision
        else -> null
    }
    classifier?.let { versionName += "-$it" }
    return versionName
}
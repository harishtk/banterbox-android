@file:Suppress("DSL_SCOPE_VIOLATION")

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url="https://repo1.maven.org/maven2")
    }

    dependencies {
        classpath(libs.android.gradlePlugin)
        // classpath(libs.google.services.gradlePlugin)
        classpath(libs.kotlin.gradlePlugin)
        classpath(libs.hilt.android.gradlePlugin)
        classpath(libs.androidx.navigation.safeargs.gradlePlugin)
        // classpath(libs.firebase.crashlytics.gradle)
        classpath(libs.dokka.gradlePlugin)
    }
}

/**
 * We need to suppress the scope violation as the IDEA has a bug
 * see [Version catalog accessors for plugin aliases shown as errors in IDE kotlin script editor #22797](https://github.com/gradle/gradle/issues/22797)
 */
plugins {
    alias(libs.plugins.android.application) apply(false)
    alias(libs.plugins.android.library) apply(false)
    alias(libs.plugins.android.nav.safeargs) apply(false)
    alias(libs.plugins.android.test) apply(false)
    // alias(libs.plugins.firebase.crashlytics) apply(false)
    // alias(libs.plugins.google.services) apply(false)
    alias(libs.plugins.protobuf) apply(false)
    alias(libs.plugins.hilt) apply(false)
    alias(libs.plugins.kapt) apply(false)
    alias(libs.plugins.ksp) apply(false)
    alias(libs.plugins.kotlin) apply(false)
    alias(libs.plugins.dokka) apply(false)
}
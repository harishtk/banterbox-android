@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin)
    // alias(libs.plugins.protobuf)
}

android {
    namespace = "space.banterbox.core.datastore"
    compileSdk = libs.versions.compileSdk.get().toIntOrNull()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }
}

dependencies {

    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.protobuf.java.lite)

    implementation(libs.kotlinx.coroutines.android)

    /* Hilt */
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    testImplementation(libs.junit)

    androidTestImplementation(testLibs.androidx.test.runner)
}

/*
protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }

    generateProtoTasks {
        all().each { task ->
            task.builtins {
                java {
                    option 'lite'
                }
                koltin {
                    option 'lite'
                }
            }
        }
    }
}*/

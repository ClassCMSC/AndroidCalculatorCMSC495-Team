// app/build.gradle.kts
// Purpose: configure the actual Android app module (:app)

plugins {
    alias(libs.plugins.android.application) // Builds an Android APK
    alias(libs.plugins.kotlin.compose)      // Enables Compose + Kotlin tooling
}

android {
    namespace = "com.example.calculator_cmsc495" // Kotlin package namespace for generated R, etc.
    compileSdk = 36                               // Android API level to compile against

    defaultConfig {
        applicationId = "com.example.calculator_cmsc495" // Unique app id installed on device
        minSdk = 24                                      // Lowest Android version supported
        targetSdk = 36                                   // Targeted Android API behavior
        versionCode = 1                                  // Internal version for Play Store
        versionName = "1.0"                              // User-facing version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Keep false while developing; avoids shrinker surprises
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Java/Kotlin bytecode compatibility
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true // Turns on Jetpack Compose support
    }
}

dependencies {
    // ...your existing deps...

    // Provides XML styles like Theme.Material3.DayNight.NoActionBar
    implementation("com.google.android.material:material:1.12.0")

    // Core Android + lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM keeps Compose libraries in sync
    implementation(platform(libs.androidx.compose.bom))

    // Compose UI building blocks
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Material 3 components
    implementation(libs.androidx.compose.material3)

    // Debug-only tooling (preview/inspector + test manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Unit + instrumented testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

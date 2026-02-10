plugins {
    // Android application plugin (builds an APK)
    alias(libs.plugins.android.application)

    // Kotlin + Compose compiler/plugin support
    alias(libs.plugins.kotlin.compose)
}

android {
    // Package namespace for R, BuildConfig, etc.
    namespace = "com.example.calculator_cmsc495"

    // API level used to compile against
    compileSdk = 36

    defaultConfig {
        // App ID installed on device/emulator (must match your manifest/activity package)
        applicationId = "com.example.calculator_cmsc495"

        // Lowest Android version supported
        minSdk = 24

        // Target Android version behavior expectations
        targetSdk = 36

        // Versioning for Play Store / releases
        versionCode = 1
        versionName = "1.0"

        // Default instrumentation runner for androidTest
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Turn on later if you want shrinking/obfuscation
            isMinifyEnabled = false

            // ProGuard/R8 rules used for release builds
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Java bytecode level (Compose + modern libs are fine on 11)
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        // Enables Jetpack Compose in this module
        compose = true
    }
}

dependencies {
    // Core Android Kotlin extensions + lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Activity + Compose integration (setContent, etc.)
    implementation(libs.androidx.activity.compose)

    // Compose BOM keeps versions aligned
    implementation(platform(libs.androidx.compose.bom))

    // Compose UI building blocks
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)

    // Preview support inside Android Studio
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Material 3 components (Buttons, Cards, Text, etc.)
    implementation(libs.androidx.compose.material3)

    // Debug-only tooling (Layout Inspector / preview tooling)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Local JVM unit testing
    testImplementation(libs.junit)

    // Instrumented Android testing (runs on device/emulator)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

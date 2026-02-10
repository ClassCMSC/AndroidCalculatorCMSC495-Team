// Root build.gradle.kts
// Purpose: declare plugins for submodules only (do NOT build an app here)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

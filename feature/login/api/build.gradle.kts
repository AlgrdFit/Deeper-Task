plugins {
    id("deepertask.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.deeper.deepertask.feature.login.api"
}

dependencies {
    api(projects.core.navigation)

    implementation(libs.kotlinx.serialization.core)
}

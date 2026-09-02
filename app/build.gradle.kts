plugins {
    id("deepertask.android.application")
    id("deepertask.android.compose")
    id("deepertask.android.hilt")
}

android {
    namespace = "com.deeper.deepertask"

    defaultConfig {
        applicationId = "com.deeper.deepertask"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.navigation)
    implementation(projects.feature.login.impl)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
}
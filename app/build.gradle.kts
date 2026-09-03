plugins {
    id("deepertask.android.application")
    id("deepertask.android.compose")
    id("deepertask.android.hilt")
    alias(libs.plugins.secrets.gradle.plugin)
}

android {
    namespace = "com.deeper.deepertask"

    defaultConfig {
        applicationId = "com.deeper.deepertask"
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures.buildConfig = true

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
    implementation(projects.feature.bathymetry.impl)
    implementation(projects.feature.scans.impl)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
}

secrets {
    defaultPropertiesFileName = "local.defaults.properties"
}

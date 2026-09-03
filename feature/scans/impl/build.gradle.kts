plugins {
    id("deepertask.android.feature")
}

android {
    namespace = "com.deeper.deepertask.feature.scans.impl"
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.designsystem)
    implementation(projects.core.navigation)
    implementation(projects.feature.bathymetry.api)
    api(projects.feature.scans.api)

    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}

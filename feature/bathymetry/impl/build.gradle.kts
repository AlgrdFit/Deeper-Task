plugins {
    id("deepertask.android.feature")
}

android {
    namespace = "com.deeper.deepertask.feature.bathymetry.impl"
}

dependencies {
    implementation(projects.core.coroutines)
    implementation(projects.core.database)
    implementation(projects.core.designsystem)
    implementation(projects.core.navigation)
    implementation(projects.core.network)
    api(projects.feature.bathymetry.api)
    implementation(projects.feature.login.api)

    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.gson)
    implementation(libs.maps.compose)
}

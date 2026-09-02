plugins {
    id("deepertask.android.feature")
}

android {
    namespace = "com.deeper.deepertask.feature.scans.impl"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.navigation)
    api(projects.feature.scans.api)
}

plugins {
    id("deepertask.android.library")
    id("deepertask.android.hilt")
}

android {
    namespace = "com.deeper.deepertask.core.coroutines"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}

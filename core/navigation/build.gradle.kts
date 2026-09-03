plugins {
    id("deepertask.android.library")
}

android {
    namespace = "com.deeper.deepertask.core.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
}

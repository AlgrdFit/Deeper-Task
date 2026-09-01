plugins {
    id("deepertask.android.library")
    id("deepertask.android.hilt")
}

val defaultBaseUrl = "https://bathus.staging.deeper.eu/api/"

android {
    namespace = "com.deeper.deepertask.core.network"

    buildFeatures.buildConfig = true

    defaultConfig {
        buildConfigField("String", "BASE_URL", "\"$defaultBaseUrl\"")
    }
}

dependencies {
    api(libs.retrofit.core)
    api(platform(libs.okhttp.bom))
    api(libs.okhttp.core)

    implementation(libs.retrofit.gson.converter)
    implementation(libs.gson)
    implementation(libs.okhttp.logging)
}

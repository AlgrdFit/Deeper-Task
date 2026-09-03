plugins {
    id("deepertask.android.library")
    id("deepertask.android.hilt")
}

android {
    namespace = "com.deeper.deepertask.core.database"
}

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    add("ksp", libs.androidx.room.compiler)
}

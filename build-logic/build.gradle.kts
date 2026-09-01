import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "com.deeper.deepertask.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.hilt.gradle.plugin)
    implementation(libs.kotlin.compose.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "deepertask.android.application"
            implementationClass = "com.deeper.deepertask.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "deepertask.android.library"
            implementationClass = "com.deeper.deepertask.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "deepertask.android.compose"
            implementationClass = "com.deeper.deepertask.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "deepertask.android.hilt"
            implementationClass = "com.deeper.deepertask.buildlogic.AndroidHiltConventionPlugin"
        }
        register("androidFeature") {
            id = "deepertask.android.feature"
            implementationClass = "com.deeper.deepertask.buildlogic.AndroidFeatureConventionPlugin"
        }
    }
}

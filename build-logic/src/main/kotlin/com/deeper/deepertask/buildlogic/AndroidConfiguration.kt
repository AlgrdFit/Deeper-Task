package com.deeper.deepertask.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

private const val COMPILE_SDK = 37
private const val MIN_SDK = 26
private const val TARGET_SDK = 37
private const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow {
        IllegalStateException("Missing '$alias' library alias in the version catalog")
    }

internal fun Project.configureAndroidApplication() {
    extensions.configure<ApplicationExtension> {
        compileSdk {
            version = release(COMPILE_SDK)
        }
        defaultConfig {
            minSdk {
                version = release(MIN_SDK)
            }
            targetSdk {
                version = release(TARGET_SDK)
            }
            testInstrumentationRunner = TEST_INSTRUMENTATION_RUNNER
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }
}

internal fun Project.configureAndroidLibrary() {
    extensions.configure<LibraryExtension> {
        compileSdk {
            version = release(COMPILE_SDK)
        }
        defaultConfig {
            minSdk {
                version = release(MIN_SDK)
            }
            testInstrumentationRunner = TEST_INSTRUMENTATION_RUNNER
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }
}

internal fun Project.configureUnitTestDependencies() {
    val catalog = libs
    dependencies {
        add("testImplementation", catalog.library("junit"))
        add("testImplementation", catalog.library("mockk"))
        add("testImplementation", catalog.library("kotlinx-coroutines-test"))
    }
}

internal fun DependencyHandler.addCatalogLibrary(
    configuration: String,
    catalog: VersionCatalog,
    alias: String,
) {
    add(configuration, catalog.library(alias))
}

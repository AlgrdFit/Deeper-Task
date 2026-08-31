package com.deeper.deepertask.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        pluginManager.withPlugin("com.android.application") {
            extensions.configure<ApplicationExtension> {
                buildFeatures.compose = true
            }
        }
        pluginManager.withPlugin("com.android.library") {
            extensions.configure<LibraryExtension> {
                buildFeatures.compose = true
            }
        }

        val catalog = libs
        dependencies {
            add("implementation", platform(catalog.library("androidx-compose-bom")))
            addCatalogLibrary("implementation", catalog, "androidx-compose-material3")
            addCatalogLibrary("implementation", catalog, "androidx-compose-ui")
            addCatalogLibrary("implementation", catalog, "androidx-compose-ui-graphics")
            addCatalogLibrary("implementation", catalog, "androidx-compose-ui-tooling-preview")
            addCatalogLibrary("debugImplementation", catalog, "androidx-compose-ui-tooling")
        }
    }
}

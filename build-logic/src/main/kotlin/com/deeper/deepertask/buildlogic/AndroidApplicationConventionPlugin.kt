package com.deeper.deepertask.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")

        configureAndroidApplication()
        configureUnitTestDependencies()
    }
}

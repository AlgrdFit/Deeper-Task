package com.deeper.deepertask.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("deepertask.android.library")
        pluginManager.apply("deepertask.android.compose")
        pluginManager.apply("deepertask.android.hilt")
    }
}

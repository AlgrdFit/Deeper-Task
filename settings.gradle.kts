pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DeeperTask"

fun includeModulesUnder(group: String) {
    rootDir.resolve(group)
        .walkTopDown()
        .onEnter { directory ->
            directory.name != "build" && !directory.name.startsWith(".")
        }
        .filter { file -> file.isFile && file.name == "build.gradle.kts" }
        .map { buildFile ->
            buildFile.parentFile
                .relativeTo(rootDir)
                .invariantSeparatorsPath
                .replace('/', ':')
        }
        .sorted()
        .forEach { modulePath -> include(":$modulePath") }
}

include(":app")
includeModulesUnder("core")
includeModulesUnder("feature")

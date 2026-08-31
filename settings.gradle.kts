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
        .listFiles()
        ?.asSequence()
        ?.filter { directory ->
            directory.isDirectory && directory.resolve("build.gradle.kts").isFile
        }
        ?.sortedBy { directory -> directory.name }
        ?.forEach { directory -> include(":$group:${directory.name}") }
}

include(":app")
includeModulesUnder("core")
includeModulesUnder("feature")

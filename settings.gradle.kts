pluginManagement {
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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Navroute"
include(":navroute-annotations")
include(":navroute-runtime")
include(":navroute-ksp")
include(":navroute-deeplink")
include(":navroute-ui")
include(":navroute-di-koin")
include(":samples:app-hilt")
include(":samples:app-koin")
include(":samples:feature:hilt:api")
include(":samples:feature:hilt:impl")
include(":samples:feature:koin:api")
include(":samples:feature:koin:impl")
 

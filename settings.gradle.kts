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
include(":samples:navigation")
include(":samples:app-hilt")
include(":samples:app-koin")
include(":samples:feature:hilt:main:api")
include(":samples:feature:hilt:main:impl")
include(":samples:feature:hilt:detail:api")
include(":samples:feature:hilt:detail:impl")
include(":samples:feature:koin:main:api")
include(":samples:feature:koin:main:impl")
include(":samples:feature:koin:detail:api")
include(":samples:feature:koin:detail:impl")
 

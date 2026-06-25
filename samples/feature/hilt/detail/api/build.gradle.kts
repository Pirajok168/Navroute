plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.pirajok.navroute.sample.hilt.detail.api"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        minSdk = 24
    }
}

ksp {
    arg("navroute.di", "none")
    arg("navroute.moduleName", "hiltDetailApi")
}

dependencies {
    implementation(project(":navroute-annotations"))
    implementation(project(":navroute-runtime"))
    implementation(project(":navroute-deeplink"))
    implementation(libs.kotlinx.serialization.core)
    ksp(project(":navroute-ksp"))
}

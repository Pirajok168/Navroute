plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "dev.pirajok.navroute.di.koin"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    api(project(":navroute-deeplink"))
    api(project(":navroute-runtime"))
    api(libs.koin.android)
    api(libs.koin.compose.navigation3)
}

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "dev.pirajok.navroute.deeplink"
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
    api(project(":navroute-runtime"))
    implementation(libs.kotlinx.serialization.core)
}

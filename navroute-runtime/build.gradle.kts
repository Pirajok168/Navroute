plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "dev.pirajok.navroute.runtime"
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
    api(libs.androidx.navigation3.runtime)
}

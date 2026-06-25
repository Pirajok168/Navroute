plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.pirajok.navroute.sample.navigation"
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
    implementation(project(":navroute-runtime"))
    implementation(libs.androidx.navigation3.runtime)
}

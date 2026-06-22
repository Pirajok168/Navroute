plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.pirajok.navroute.ui"
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
    implementation(platform(libs.androidx.compose.bom))
    api(project(":navroute-annotations"))
    api(project(":navroute-runtime"))
    api(libs.androidx.navigation3.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
}

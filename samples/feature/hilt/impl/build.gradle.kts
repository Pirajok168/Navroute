plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.pirajok.navroute.sample.impl"
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
    arg("navroute.di", "hilt")
    arg("navroute.moduleName", "sample")
}

dependencies {
    implementation(project(":navroute-annotations"))
    implementation(project(":navroute-runtime"))
    implementation(project(":navroute-deeplink"))
    implementation(project(":navroute-ui"))
    implementation(project(":samples:feature:hilt:api"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.hilt.android)
    ksp(project(":navroute-ksp"))
}

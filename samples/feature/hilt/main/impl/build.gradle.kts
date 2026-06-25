plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.pirajok.navroute.sample.hilt.main.impl"
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
    arg("navroute.moduleName", "hiltMain")
}

dependencies {
    implementation(project(":navroute-annotations"))
    implementation(project(":navroute-runtime"))
    implementation(project(":navroute-deeplink"))
    implementation(project(":navroute-ui"))
    implementation(project(":samples:navigation"))
    implementation(project(":samples:feature:hilt:main:api"))
    implementation(project(":samples:feature:hilt:detail:api"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.hilt.android)
    ksp(project(":navroute-ksp"))
}

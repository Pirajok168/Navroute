plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.pirajok.navroute.sample.koin.main.impl"
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
    arg("navroute.di", "koin")
    arg("navroute.moduleName", "koinMain")
}

dependencies {
    implementation(project(":navroute-annotations"))
    implementation(project(":navroute-runtime"))
    implementation(project(":navroute-deeplink"))
    implementation(project(":navroute-di-koin"))
    implementation(project(":navroute-ui"))
    implementation(project(":samples:navigation"))
    implementation(project(":samples:feature:koin:main:api"))
    implementation(project(":samples:feature:koin:detail:api"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    ksp(project(":navroute-ksp"))
}

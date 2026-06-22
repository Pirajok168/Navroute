plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":navroute-annotations"))
    implementation(libs.kotlinpoet)
    implementation(libs.ksp.api)
}

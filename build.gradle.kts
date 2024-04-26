plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.dokka).apply(false)
    alias(libs.plugins.spotless)
}

group = "com.godaddy"
version = "0.5.0"

apply("${project.rootDir}/gradle/spotless.gradle")
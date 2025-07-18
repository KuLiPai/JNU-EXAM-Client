plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
}

subprojects {
    configurations.all {
        resolutionStrategy {
            force("com.squareup.javapoet:javapoet:1.13.0")
        }
    }
}
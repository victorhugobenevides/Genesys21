import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    kotlin("android")
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "com.itbenevides.genesys21.screenshot"
    // Atualizado de 34 para 36 conforme erro de AAR metadata
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Dependências do módulo composeApp
    implementation(project(":composeApp"))
    implementation(project(":shared"))

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Testes
    testImplementation(libs.junit)
    testImplementation("app.cash.paparazzi:paparazzi:1.3.5")
    testImplementation("app.cash.paparazzi:paparazzi-annotations:1.3.5")
    
    // Compose Testing
    testImplementation("androidx.compose.ui:ui-test-junit4:1.7.3")
    testImplementation("androidx.compose.ui:ui-test:1.7.3")
    testImplementation("androidx.compose.runtime:runtime:1.7.3")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

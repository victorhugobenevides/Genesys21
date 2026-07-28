plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.paparazzi)
}

kotlin {
    androidTarget()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.composeApp)
                implementation(projects.shared)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)

                // Compose Dependencies
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.materialIconsExtended)
                implementation(libs.mockk)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.activity.compose)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.junit)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                // Paparazzi needs these in the test classpath
                implementation(libs.junit)
            }
        }
    }
}

android {
    namespace = "com.itbenevides.genesys21.screenshot"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }
}

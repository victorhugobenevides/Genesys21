import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm()
    
    // Alvos iOS configurados apenas como bibliotecas (sem framework próprio)
    // para evitar conflito de resolução de KLIB no umbrella framework (ComposeApp)
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.clientSerialization)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.firebase.auth.kmp)
        }
        
        val iosMain by getting {
            dependencies {
                implementation(libs.firebase.auth.kmp)
            }
        }
        
        jsMain.dependencies {
            implementation(libs.firebase.auth.kmp)
        }

        wasmJsMain.dependencies {
            // JS Interop manual
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
            implementation(libs.ktor.clientMock)
        }
    }
}

android {
    namespace = "com.itbenevides.genesys21.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

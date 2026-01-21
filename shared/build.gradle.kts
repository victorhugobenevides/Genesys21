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
            // Removido analytics do commonMain para não quebrar o WasmJs
        }

        androidMain.dependencies {
            implementation(libs.firebase.auth.kmp)
            implementation("dev.gitlive:firebase-analytics:2.4.0") // Adicionado aqui
        }
        
        val iosMain by getting {
            dependencies {
                implementation(libs.firebase.auth.kmp)
                implementation("dev.gitlive:firebase-analytics:2.4.0") // Adicionado aqui
            }
        }
        
        jsMain.dependencies {
            implementation(libs.firebase.auth.kmp)
            implementation("dev.gitlive:firebase-analytics:2.4.0") // Adicionado aqui (opcional para JS)
        }

        wasmJsMain.dependencies {
            // WasmJs continua via Interop JS puro (Analytics.wasmJs.kt)
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

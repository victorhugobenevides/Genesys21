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
            // Usamos api para que os módulos que dependem de :shared (como :composeApp) 
            // também tenham acesso ao kotlinx-datetime sem precisar declará-lo novamente,
            // evitando assim conflitos de vinculação IR no Wasm.
            api(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.firebase.auth.kmp)
            implementation("dev.gitlive:firebase-analytics:2.1.0")
        }
        
        val iosMain by getting {
            dependencies {
                implementation(libs.firebase.auth.kmp)
                implementation("dev.gitlive:firebase-analytics:2.1.0")
            }
        }
        
        jsMain.dependencies {
            implementation(libs.firebase.auth.kmp)
            implementation("dev.gitlive:firebase-analytics:2.1.0") 
        }

        wasmJsMain.dependencies {
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
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

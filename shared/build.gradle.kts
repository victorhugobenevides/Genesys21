import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.serialization)
    id("jacoco")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm()
    iosX64(); iosArm64(); iosSimulatorArm64()
    js { browser() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.clientSerialization)
            implementation(libs.koin.core)
            api(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.firebase.auth.kmp)
            implementation("dev.gitlive:firebase-analytics:2.1.0")
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
    
    defaultConfig {
        minSdk = 23
    }

    buildTypes {
        getByName("debug") {
            enableUnitTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

jacoco { toolVersion = "0.8.12" }

tasks.register<JacocoReport>("jacocoSharedTestReport") {
    dependsOn("testDebugUnitTest")
    group = "Reporting"
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.projectDirectory.dir("jacoco-reports/html"))
        xml.outputLocation.set(layout.projectDirectory.file("jacoco-reports/report.xml"))
    }

    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "**/di/**", "**/*Serializer*.*"
    )
    
    val classTree = fileTree(layout.buildDirectory.dir("classes/kotlin/android/debug")) { exclude(fileFilter) }
    sourceDirectories.setFrom(files("src/commonMain/kotlin", "src/androidMain/kotlin"))
    classDirectories.setFrom(files(classTree))
    executionData.setFrom(fileTree(layout.buildDirectory) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
    })
}

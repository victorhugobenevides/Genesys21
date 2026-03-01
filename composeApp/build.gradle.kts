import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.kotlin.serialization)
    id("jacoco")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            export(projects.shared)
        }
    }
    
    js { browser(); binaries.executable() }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static(project.file("src/webMain/resources").canonicalPath)
                    proxy = mutableListOf(
                        KotlinWebpackConfig.DevServer.Proxy(mutableListOf("/api", "/uploads", "/upload"), "http://localhost:8080")
                    )
                }
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)
            api(projects.shared)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.clientSerialization)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.firebase.auth.kmp)
            implementation(libs.kmpauth.google)
            implementation(libs.kmpauth.firebase)
            implementation(libs.kmpauth.uihelper)
            implementation(libs.koin.android)
            implementation(libs.peekaboo.image.picker)
            implementation(libs.firebase.crashlytics)
            implementation(libs.firebase.perf)
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.koin.test)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
                implementation("io.mockk:mockk:1.13.13")
            }
        }

        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.androidx.testExt.junit)
                implementation(libs.androidx.espresso.core)
                implementation(libs.koin.test)
                implementation("org.jetbrains.compose.ui:ui-test-junit4:1.7.3")
                implementation("androidx.compose.ui:ui-test-junit4:1.7.3")
                implementation("androidx.compose.ui:ui-test-manifest:1.7.3")
                implementation("io.mockk:mockk-android:1.13.13")
            }
        }
    }
}

android {
    namespace = "com.itbenevides.genesys21"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "com.itbenevides.genesys21"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2
        versionName = "1.1"
        testInstrumentationRunner = "com.itbenevides.genesys21.GenesysTestRunner"
        buildConfigField("String", "WEB_BASE_URL", "\"http://localhost:8081\"")
    }
    
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.all {
            it.configure<JacocoTaskExtension> {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
    }

    buildFeatures { buildConfig = true }
    packaging { 
        resources { 
            excludes += "/META-INF/{AL2.0,LGPL2.1}" 
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        } 
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
            buildConfigField("String", "WEB_BASE_URL", "\"http://localhost:8081\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

jacoco { toolVersion = "0.8.12" }

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "Reporting"
    description = "Gera relatório de cobertura para testes unitários Android do composeApp"

    // Depende dos testes unitários debug
    dependsOn("testDebugUnitTest")

    // Limpa o diretório de relatórios antes de gerar novo
    doFirst {
        delete(layout.projectDirectory.dir("jacoco-reports"))
    }

    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.projectDirectory.dir("jacoco-reports/html"))
        xml.outputLocation.set(layout.projectDirectory.file("jacoco-reports/report.xml"))
    }

    val fileFilter = listOf(
        // Classes geradas pelo Android
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/generated/**",
        // Classes de teste
        "**/*Test*.*", "android/**/*.*",
        // Classes geradas pelo Kotlin/Compose
        "**/Lambda$*.class", "**/Lambda.class",
        "**/*Lambda.class", "**/*Lambda*.class", "**/*\$serializer.class",
        "**/ComposableSingletons$*.*",
        // Outras plataformas (não Android)
        "**/*_desktop*.*",
        // Bibliotecas externas - excluir do relatório
        "**/androidx/**", "**/com/google/**", "**/com/android/**",
        "**/org/koin/**", "**/org/jetbrains/**", "**/io/ktor/**",
        "**/kotlin/**", "**/kotlinx/**", "**/javax/**", "**/java/**",
        "**/dalvik/**", "**/libcore/**",
        // UI Components Compose - testados por UI tests, não unit tests
        "**/ui/components/**",
        // Screens Compose - excluir somente os arquivos de tela (Kt), manter State/Event
        "**/*ScreenKt*", "**/*Screen$*",
        "**/*ComponentEditor*", "**/*Dialog*", "**/*EditorScreen*",
        "**/*Renderer*", "**/*Controls*",
        // Theme e recursos - não precisam de testes unitários
        "**/ui/theme/**", "**/di/**"
    )

    val buildDir = layout.buildDirectory.asFile.get()

    // Configuração dos diretórios de classes para o JaCoCo
    // Inclui apenas as classes do projeto (com.itbenevides), excluindo bibliotecas externas
    classDirectories.setFrom(
        fileTree("$buildDir/intermediates/classes/debug/transformDebugClassesWithAsm/dirs") {
            include("com/itbenevides/**")
            exclude(fileFilter)
        }
    )

    sourceDirectories.setFrom(
        files(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/androidUnitTest/kotlin"
        )
    )

    // Caminho específico para o arquivo de execução JaCoCo
    // Usa o arquivo gerado pela task testDebugUnitTest
    executionData.setFrom(
        files("$buildDir/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
    )
}

dependencies {
    debugImplementation("org.jetbrains.compose.ui:ui-tooling:1.7.3")
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.auth.ktx)
}

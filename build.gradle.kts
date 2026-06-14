plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    id("idea")
}

subprojects {
    apply(plugin = rootProject.libs.plugins.ktlint.get().pluginId)
    
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        filter {
            exclude { it.file.path.contains("generated") }
        }
    }
}

// O erro :kotlinStoreYarnLock FAILED é mitigado via gradle.properties:
// kotlin.js.yarn.lockFileRetentionPolicy=STORE_NONE

idea {
    module {
        excludeDirs.add(file("deploy"))
        excludeDirs.add(file("kotlin-js-store"))
    }
}

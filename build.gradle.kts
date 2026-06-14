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
    id("jacoco")
    id("idea")
}

subprojects {
    apply(plugin = rootProject.libs.plugins.ktlint.get().pluginId)
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)
    apply(plugin = "jacoco")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        filter {
            exclude { it.file.path.contains("generated") }
        }
    }

    // Configuração de detekt usando arquivo customizado
    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        parallel = true
        config.setFrom(files("${rootProject.projectDir}/.speckit/quality/detekt-config.yml"))
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

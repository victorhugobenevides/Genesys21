plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    application
    id("jacoco")
}

group = "com.itbenevides.genesys21"
version = "1.0.0"

application {
    mainClass.set("com.itbenevides.genesys21.ApplicationKt")
}

// RESOLUÇÃO DE CONFLITO: Substitui google-collections por guava para evitar erro de compatibilidade
configurations.all {
    resolutionStrategy {
        dependencySubstitution {
            substitute(module("com.google.collections:google-collections")).using(module("com.google.guava:guava:33.4.0-jre"))
        }
    }
}

jacoco { toolVersion = "0.8.12" }

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverAuth)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverCors)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.caching.headers)
    implementation(libs.ktor.serializationJson)
    implementation(libs.firebase.admin)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation(libs.sqlite.jdbc)
    implementation(libs.hikaricp)
    implementation(libs.thumbnailator)
    implementation(libs.mercadopago.sdk.java)
    implementation(libs.dotenv.kotlin)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

tasks.register<JacocoReport>("jacocoServerTestReport") {
    dependsOn("test")
    group = "Reporting"
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.projectDirectory.dir("jacoco-reports/html"))
        xml.outputLocation.set(layout.projectDirectory.file("jacoco-reports/report.xml"))
    }
    // Incluir todas as classes exceto ApplicationKt e generated
    val fileFilter = listOf(
        "**/ApplicationKt*",
        "**/generated/**"
    )
    val classTree = fileTree(layout.buildDirectory.dir("classes/kotlin/main")) {
        exclude(fileFilter)
    }
    sourceDirectories.setFrom(files("src/main/kotlin"))
    classDirectories.setFrom(files(classTree))
    executionData.setFrom(fileTree(layout.buildDirectory) {
        include("jacoco/test.exec")
    })
}

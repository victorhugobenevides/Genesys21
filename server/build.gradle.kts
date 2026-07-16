plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "com.itbenevides.genesys21"
version = "1.0.0"
application {
    mainClass.set("com.itbenevides.genesys21.ApplicationKt")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverAuth)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverCors)

    // CORREÇÃO: Usando string direta para garantir que o build não falhe por falta no catálogo
    implementation("io.ktor:ktor-server-status-pages:3.0.3")

    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.caching.headers)

    implementation(libs.ktor.serializationJson)
    implementation(libs.firebase.admin)

    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.59.0")
    implementation(libs.sqlite.jdbc)
    implementation(libs.hikaricp)

    // Manipulação de Imagem
    implementation(libs.thumbnailator)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

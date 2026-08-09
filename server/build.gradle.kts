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

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverAuth)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverCors)
    implementation(libs.ktor.serverRateLimit)
    implementation(libs.ktor.serverDefaultHeaders)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.ktor.serverForwardedHeader)

    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.caching.headers)

    implementation(libs.ktor.serializationJson)
    implementation(libs.firebase.admin)

    implementation(libs.ktor.clientCore)
    implementation(libs.ktor.clientJava)
    implementation(libs.ktor.clientContentNegotiation)

    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.59.0")
    implementation(libs.sqlite.jdbc)
    implementation(libs.hikaricp)

    // Manipulação de Imagem
    implementation(libs.thumbnailator)

    // Pagamentos
    implementation(libs.stripe.java)

    // Google Meet / Calendar (String literals used due to temporary Gradle accessor synchronization issue)
    implementation("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0")
    implementation("com.google.api-client:google-api-client:2.0.0")
    implementation("com.google.http-client:google-http-client-gson:1.42.3")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

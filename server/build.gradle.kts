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
    implementation(libs.ktor.serverCors) // Adicionado aqui
    implementation(libs.ktor.serializationJson)
    implementation(libs.firebase.admin)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

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
    
    // CORREÇÃO: Usando dependências do catálogo core ou strings diretas se necessário
    // Se libs.ktor.serverCompression não existir, o Ktor Core já traz o básico,
    // mas plugins específicos precisam ser adicionados.
    // Vamos assumir que o catálogo tem 'ktor-server-compression' e 'ktor-server-caching-headers'
    // Se não tiver, use: implementation("io.ktor:ktor-server-compression:3.0.0")
    
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.caching.headers)
    
    implementation(libs.ktor.serializationJson)
    implementation(libs.firebase.admin)
    
    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation(libs.sqlite.jdbc)
    implementation(libs.hikaricp) // POOL DE CONEXÕES
    
    // Manipulação de Imagem
    implementation(libs.thumbnailator)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

package com.itbenevides.genesys21

actual fun initializeFirebase() {
    // A inicialização é feita via ponte JavaScript no index.html (firebase-bridge.js)
    println("WASM: Autenticação configurada via Ponte JavaScript.")
}

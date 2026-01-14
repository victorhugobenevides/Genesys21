package com.itbenevides.genesys21

actual fun initializeFirebase() {
    // WASM não suporta o Firebase nativo no momento
    println("WASM: Inicialização do Firebase ignorada (Não suportado)")
}

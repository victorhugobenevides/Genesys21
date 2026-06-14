package com.itbenevides.genesys21

actual fun initializeFirebase() {
    // No Android, o Firebase é inicializado automaticamente pelo ContentProvider
    // injetado pelo plugin do Google Services.
}

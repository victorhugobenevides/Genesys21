package com.itbenevides.genesys21.screenshot.base

import java.io.File
import java.util.Base64

/**
 * Fornece imagens mockadas locais para testes Paparazzi.
 * Evita dependência de rede e garante renderização no relatório.
 */
object TestImageProvider {
    private val imageBytes: ByteArray by lazy {
        Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII="
        )
    }

    private val tempImageFile: File by lazy {
        val file = File.createTempFile("paparazzi-mock", ".png")
        file.writeBytes(imageBytes)
        file.deleteOnExit()
        file
    }

    fun mockImageUrl(): String = tempImageFile.toURI().toString()
}

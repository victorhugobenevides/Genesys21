package com.itbenevides.genesys21.util

private const val BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

fun ByteArray.toBase64(): String {
    val result = StringBuilder()
    var i = 0
    while (i < size) {
        val b0 = this[i++].toInt() and 0xFF
        if (i < size) {
            val b1 = this[i++].toInt() and 0xFF
            if (i < size) {
                val b2 = this[i++].toInt() and 0xFF
                result.append(BASE64_ALPHABET[b0 shr 2])
                result.append(BASE64_ALPHABET[(b0 shl 4 or (b1 shr 4)) and 0x3F])
                result.append(BASE64_ALPHABET[(b1 shl 2 or (b2 shr 6)) and 0x3F])
                result.append(BASE64_ALPHABET[b2 and 0x3F])
            } else {
                result.append(BASE64_ALPHABET[b0 shr 2])
                result.append(BASE64_ALPHABET[(b0 shl 4 or (b1 shr 4)) and 0x3F])
                result.append(BASE64_ALPHABET[(b1 shl 2) and 0x3F])
                result.append('=')
            }
        } else {
            result.append(BASE64_ALPHABET[b0 shr 2])
            result.append(BASE64_ALPHABET[(b0 shl 4) and 0x3F])
            result.append("==")
        }
    }
    return result.toString()
}

object Base64Decoder {
    private val DECODE_TABLE = IntArray(256) { -1 }

    init {
        for (i in 0 until 64) {
            DECODE_TABLE[BASE64_ALPHABET[i].code] = i
        }
    }

    fun decode(base64: String): ByteArray {
        val clean = base64.replace("\r", "").replace("\n", "").replace(" ", "").trimEnd('=')
        val outputSize = (clean.length * 3) / 4
        val result = ByteArray(outputSize)
        var buffer = 0
        var bufferBits = 0
        var resultIndex = 0

        for (char in clean) {
            val value = DECODE_TABLE[char.code]
            if (value == -1) continue
            buffer = (buffer shl 6) or value
            bufferBits += 6
            if (bufferBits >= 8) {
                bufferBits -= 8
                if (resultIndex < outputSize) {
                    result[resultIndex++] = (buffer shr bufferBits).toByte()
                }
            }
        }
        return result
    }
}

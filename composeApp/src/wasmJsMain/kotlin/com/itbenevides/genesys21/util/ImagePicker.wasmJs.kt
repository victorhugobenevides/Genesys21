package com.itbenevides.genesys21.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader

@Composable
actual fun rememberImagePicker(onResult: (ByteArray?) -> Unit): () -> Unit {
    return remember {
        {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = "image/*"

            input.onchange = {
                val files = input.files
                if (files != null && files.length > 0) {
                    val file = files.item(0)
                    if (file != null) {
                        val reader = FileReader()
                        reader.onload = {
                            val arrayBuffer = reader.result as ArrayBuffer
                            val uint8Array = Uint8Array(arrayBuffer)
                            val bytes = ByteArray(uint8Array.length) { i -> uint8Array[i] }
                            onResult(bytes)
                        }
                        reader.readAsArrayBuffer(file)
                    } else {
                        onResult(null)
                    }
                } else {
                    onResult(null)
                }
            }

            input.click()
        }
    }
}

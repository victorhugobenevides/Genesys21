package com.itbenevides.genesys21.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import kotlin.js.JsAny
import kotlin.js.JsArray

@Composable
actual fun rememberFileHandler(onResult: (String?) -> Unit): () -> Unit {
    return remember {
        {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = ".benevides"

            input.onchange = {
                val files = input.files
                if (files != null && files.length > 0) {
                    val file = files.item(0)
                    if (file != null) {
                        val reader = FileReader()
                        reader.onload = {
                            val content = reader.result.toString()
                            onResult(content)
                        }
                        reader.readAsText(file)
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

actual fun downloadFile(
    content: String,
    fileName: String,
) {
    val array = JsArray<JsAny?>()
    array[0] = content.toJsString()
    val blob = Blob(array, BlobPropertyBag(type = "application/json"))
    val url = URL.createObjectURL(blob)
    val link = document.createElement("a") as HTMLAnchorElement
    link.href = url
    link.download = if (fileName.endsWith(".benevides")) fileName else "$fileName.benevides"
    link.click()
    URL.revokeObjectURL(url)
}

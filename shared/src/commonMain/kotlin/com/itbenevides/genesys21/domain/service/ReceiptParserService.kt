package com.itbenevides.genesys21.domain.service

import com.itbenevides.genesys21.domain.model.Receipt
import com.itbenevides.genesys21.domain.model.ReceiptItem
import com.itbenevides.genesys21.domain.util.NfeUrlBuilder
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class ReceiptParserService(
    private val httpClient: HttpClient? = null
) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Processa dinamicamente a Nota Fiscal.
     * Se uma API Key do Gemini for fornecida e houver internet, consulta a IA multimodal do Gemini Flash.
     * Caso contrário (ou se estiver offline), faz o parse regex/OCR instantâneo no próprio dispositivo.
     */
    suspend fun parseReceiptDynamic(
        rawText: String = "",
        imageBase64: String? = null,
        apiKey: String? = null
    ): Receipt {
        if (!apiKey.isNullOrBlank() && !imageBase64.isNullOrBlank() && httpClient != null) {
            runCatching {
                parseWithGeminiApi(imageBase64, apiKey)
            }.getOrNull()?.let { return it }
        }

        // Fallback local instantâneo (Sem custo / Offline)
        return parseReceiptFromText(rawText)
    }

    private suspend fun parseWithGeminiApi(imageBase64: String, apiKey: String): Receipt {
        val client = httpClient ?: throw IllegalStateException("HttpClient não disponível")
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

        val prompt = """
            Analise esta nota fiscal (DANFE ou NFC-e) e retorne APENAS um JSON estrito no formato abaixo.
            Importante:
            1. No campo 'emitente', use o nome fantasia da loja.
            2. Extraia todos os produtos/serviços reais da lista de itens.
            3. Ignore campos de impostos, CPF ou mensagens publicitárias na lista de itens.
            4. O campo 'chaveAcesso' deve ter exatamente 44 dígitos numéricos.

            Formato:
            {
              "emitente": "Nome da Loja",
              "cnpjEmitente": "XX.XXX.XXX/XXXX-XX",
              "dataEmissao": "DD/MM/AAAA",
              "valorTotal": 0.0,
              "categoria": "Supermercado|Eletrônicos|Farmácia|Combustível|Alimentação|Geral",
              "chaveAcesso": "44 dígitos",
              "items": [
                { "descricao": "nome do item", "quantidade": 1.0, "valorUnitario": 0.0, "valorTotal": 0.0 }
              ]
            }
        """.trimIndent()

        val requestBody = buildJsonObject {
            putJsonArray("contents") {
                addJsonObject {
                    putJsonArray("parts") {
                        addJsonObject { put("text", prompt) }
                        addJsonObject {
                            putJsonObject("inlineData") {
                                put("mimeType", "image/jpeg")
                                put("data", imageBase64)
                            }
                        }
                    }
                }
            }
        }

        val response = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(requestBody.toString())
        }

        val responseText = response.bodyAsText()
        val parsedJson = json.parseToJsonElement(responseText).jsonObject
        val textContent = parsedJson["candidates"]?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("content")?.jsonObject
            ?.get("parts")?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("text")?.jsonPrimitive?.content ?: throw IllegalArgumentException("Resposta da IA vazia")

        val cleanJsonString = textContent.replace("```json", "").replace("```", "").trim()
        val receiptObj = json.parseToJsonElement(cleanJsonString).jsonObject

        val emitente = receiptObj["emitente"]?.jsonPrimitive?.content ?: "Nota Fiscal"
        val cnpj = receiptObj["cnpjEmitente"]?.jsonPrimitive?.content
        val dataEmissao = receiptObj["dataEmissao"]?.jsonPrimitive?.content ?: "10/03/2026"
        val valorTotal = receiptObj["valorTotal"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val categoria = receiptObj["categoria"]?.jsonPrimitive?.content ?: "Geral"
        val chave = receiptObj["chaveAcesso"]?.jsonPrimitive?.content ?: NfeUrlBuilder.extractChaveAcesso(cleanJsonString)

        val itemsList = mutableListOf<ReceiptItem>()
        receiptObj["items"]?.jsonArray?.forEach { itemEl ->
            val iObj = itemEl.jsonObject
            itemsList.add(
                ReceiptItem(
                    descricao = iObj["descricao"]?.jsonPrimitive?.content ?: "Produto",
                    quantidade = iObj["quantidade"]?.jsonPrimitive?.doubleOrNull ?: 1.0,
                    valorUnitario = iObj["valorUnitario"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                    valorTotal = iObj["valorTotal"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                )
            )
        }

        val cleanKey = chave?.let { NfeUrlBuilder.extractChaveAcesso(it) }
        val onlineUrl = cleanKey?.let { NfeUrlBuilder.buildOnlineUrl(it) }

        return Receipt(
            id = "rec-" + (cleanKey ?: (emitente.hashCode().toString() + kotlinx.datetime.Clock.System.now().toEpochMilliseconds())),
            chaveAcesso = cleanKey,
            emitente = emitente,
            cnpjEmitente = cnpj,
            dataEmissao = dataEmissao,
            valorTotal = valorTotal,
            categoria = categoria,
            onlineUrl = onlineUrl,
            items = itemsList
        )
    }

    /**
     * Processa o texto extraído da Nota Fiscal via OCR/Regex local (Modo Offline/Gratuito).
     */
    fun parseReceiptFromText(rawText: String, imagePath: String? = null): Receipt {
        val chave = NfeUrlBuilder.extractChaveAcesso(rawText)
        val onlineUrl = chave?.let { NfeUrlBuilder.buildOnlineUrl(it) }

        val cnpjMatch = Regex("""\d{2}\.\d{3}\.\d{3}/\d{4}-\d{2}""").find(rawText)?.value
        val dataMatch = Regex("""\d{2}/\d{2}/\d{4}""").find(rawText)?.value ?: "10/03/2026"

        val valorMatch = Regex("""(?:VALOR TOTAL|TOTAL|VALOR).*?R?\$\s*(\d+[.,]\d{2})""", RegexOption.IGNORE_CASE)
            .find(rawText)?.groupValues?.get(1)
            ?.replace(",", ".")
            ?.toDoubleOrNull() ?: 0.0

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val emitenteCandidate = lines.firstOrNull {
            !it.contains("DANFE", ignoreCase = true) &&
            !it.contains("RECEBEMOS", ignoreCase = true) &&
            it.length in 3..40
        } ?: "Nota Fiscal Escaneada"

        return Receipt(
            id = "rec-" + (chave ?: (emitenteCandidate.hashCode().toString() + kotlinx.datetime.Clock.System.now().toEpochMilliseconds())),
            chaveAcesso = chave,
            emitente = emitenteCandidate,
            cnpjEmitente = cnpjMatch,
            dataEmissao = dataMatch,
            valorTotal = valorMatch,
            categoria = detectCategory(emitenteCandidate, rawText),
            imagePath = imagePath,
            onlineUrl = onlineUrl,
            items = extractBasicItems(rawText)
        )
    }

    private fun detectCategory(emitente: String, text: String): String {
        val lower = (emitente + " " + text).lowercase()
        return when {
            lower.contains("eletro") || lower.contains("tech") || lower.contains("celular") -> "Eletrônicos"
            lower.contains("supermercado") || lower.contains("atacad") || lower.contains("mercado") -> "Supermercado"
            lower.contains("farmacia") || lower.contains("drogaria") || lower.contains("remedio") -> "Farmácia"
            lower.contains("posto") || lower.contains("combustivel") || lower.contains("gasolina") -> "Combustível"
            lower.contains("restaurante") || lower.contains("lanchonete") || lower.contains("cafe") -> "Alimentação"
            else -> "Geral"
        }
    }

    private fun extractBasicItems(text: String): List<ReceiptItem> {
        val items = mutableListOf<ReceiptItem>()
        // Regex mais criteriosa: nome de produto (letras/numeros/espaços) seguido de um valor monetário
        val productRegex = Regex("""([A-Z0-9\s]{4,40})\s+(?:R\$\s*)?(\d+[.,]\d{2})""", RegexOption.IGNORE_CASE)

        // Termos que indicam que a linha NÃO é um produto, mas sim lixo do OCR ou metadados
        val blacklist = listOf(
            "CPF", "CNPJ", "VALOR", "TOTAL", "ICMS", "TRIBUTO", "BASE", "CALCULO",
            "CHAVE", "ACESSO", "DATA", "EMISSAO", "DESTINATARIO", "MUNICIPIO", "DISTRITO",
            "ITEM", "UNID", "PRECO", "QUANT", "DESC", "PAGAMENTO", "DINHEIRO", "TROCO",
            "VIA", "CONSUMIDOR", "FISCAL", "ELETRONICA", "DANFE", "NF-E", "NFC-E"
        )

        val matches = productRegex.findAll(text)
        for (m in matches.take(20)) {
            val name = m.groupValues[1].trim()
            val valStr = m.groupValues[2].replace(",", ".").toDoubleOrNull() ?: 0.0

            // Filtros de Qualidade
            val isBlacklisted = blacklist.any { name.contains(it, ignoreCase = true) }
            val hasDigitsOnly = name.all { it.isDigit() || it.isWhitespace() } // Provavelmente uma data ou código longo

            if (valStr > 0 && !isBlacklisted && !hasDigitsOnly && name.length >= 3) {
                items.add(ReceiptItem(
                    descricao = name.uppercase(),
                    quantidade = 1.0,
                    valorUnitario = valStr,
                    valorTotal = valStr
                ))
            }
        }
        return items
    }
}

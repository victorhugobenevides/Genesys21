package com.itbenevides.genesys21.domain.service

import com.itbenevides.genesys21.domain.model.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class PageAIGeneratorService(
    private val httpClient: HttpClient? = null,
    private val serverUrl: String? = null
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Gera uma página completa via IA com base na descrição do usuário.
     */
    suspend fun generatePage(
        prompt: String,
        apiKey: String? = null
    ): Page {
        // No cliente (Wasm/Android), encaminha para o backend para segurança da chave
        if (!serverUrl.isNullOrBlank() && httpClient != null && apiKey.isNullOrBlank()) {
            val response = httpClient.post("$serverUrl/api/public/ai/generate-page") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("prompt", prompt)
                }.toString())
            }
            if (response.status.isSuccess()) {
                return json.decodeFromString<Page>(response.bodyAsText())
            } else {
                throw Exception("Erro no servidor de IA: ${response.status}")
            }
        }

        // No backend, chama o Gemini diretamente
        if (!apiKey.isNullOrBlank()) {
            return generateWithGemini(prompt, apiKey)
        }

        throw Exception("Configuração de IA ausente.")
    }

    private suspend fun generateWithGemini(userPrompt: String, apiKey: String): Page {
        val client = httpClient ?: HttpClient()
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=$apiKey"

        val systemInstruction = """
            Você é o Mestre de Design do Genesys21. Sua missão é criar a estrutura de uma página (site) perfeita com base na descrição do usuário.

            DIRETRIZES DE DESIGN:
            1. Escolha o tema mais adequado entre: ELEGANCE (Luxo), VIBRANT (Tech), NATURE (Verdes/Artesanal), MONO (Moda/B&W), MIDNIGHT (Dark), CANDY (Pastel).
            2. Crie um título cativante para a página.
            3. Selecione e configure de 3 a 7 componentes da lista abaixo:
               - Hero: Banner principal com título, subtítulo e imagem (use imagens do Unsplash relacionadas ao tema).
               - Benefits: Grade de diferenciais (3 itens).
               - Text: Para descrições ou sobre a marca.
               - Header: Para separar seções.
               - ProductList: Use isHorizontal=true se quiser um carrossel.
               - ServiceList: Essencial se o usuário oferecer serviços.
               - SocialLinks: Inclua instagram, whatsapp e e-mail fictícios.
               - Testimonial: Uma prova social forte.
               - Grid: Para galeria de fotos do ambiente (use imagens Unsplash).

            IMPORTANTE:
            - Retorne APENAS o JSON da estrutura da classe 'Page'.
            - No campo 'id', use 'ai-gen'.
            - No campo 'storeId', use 'admin'.
            - Use URLs de imagens reais e bonitas do Unsplash (ex: https://images.unsplash.com/photo-...).

            FORMATO DO JSON ESPERADO:
            {
              "id": "ai-gen",
              "storeId": "admin",
              "title": "Título Criativo",
              "theme": "VIBRANT",
              "components": [
                {
                   "type": "com.itbenevides.genesys21.domain.model.PageComponent.Hero",
                   "title": "...",
                   "subtitle": "...",
                   "imageUrl": "...",
                   "buttonText": "..."
                }
                // ... outros componentes
              ]
            }
        """.trimIndent()

        val requestBody = buildJsonObject {
            putJsonArray("contents") {
                addJsonObject {
                    putJsonArray("parts") {
                        addJsonObject { put("text", "$systemInstruction\n\nPROMPT DO USUÁRIO: $userPrompt") }
                    }
                }
            }
        }

        val response = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(requestBody.toString())
        }

        val responseText = response.bodyAsText()
        if (!response.status.isSuccess()) throw Exception("Gemini Error: $responseText")

        val parsedJson = json.parseToJsonElement(responseText).jsonObject
        val textContent = parsedJson["candidates"]?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("content")?.jsonObject
            ?.get("parts")?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("text")?.jsonPrimitive?.content ?: throw Exception("IA retornou vazio")

        val cleanJson = textContent.replace("```json", "").replace("```", "").trim()

        // Parse manual inicial para corrigir nomes de tipos se necessário e garantir integridade
        val pageObj = json.parseToJsonElement(cleanJson).jsonObject
        val componentsArray = pageObj["components"]?.jsonArray ?: JsonArray(emptyList())

        val finalComponents = componentsArray.map { el ->
            val obj = el.jsonObject
            // Aqui poderíamos injetar o SerialName correto se a IA falhar na string longa
            el
        }

        return Page(
            id = "ai-" + com.itbenevides.genesys21.util.GenesysUUID.randomUUID().take(8),
            storeId = "genesys-official-store",
            title = pageObj["title"]?.jsonPrimitive?.content ?: "Minha Nova Página",
            theme = try { PageThemeConfig.valueOf(pageObj["theme"]?.jsonPrimitive?.content ?: "ELEGANCE") } catch(e: Exception) { PageThemeConfig.ELEGANCE },
            components = json.decodeFromJsonElement<List<PageComponent>>(JsonArray(finalComponents))
        )
    }
}

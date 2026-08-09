package com.itbenevides.genesys21.data.service

import com.itbenevides.genesys21.domain.model.Receipt
import com.itbenevides.genesys21.domain.model.ReceiptItem
import com.itbenevides.genesys21.domain.util.NfeUrlBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlinx.datetime.Clock

class SefazScraperService {

    fun parseFromUrl(url: String): Receipt? {
        return try {
            val doc = Jsoup.connect(url)
                .timeout(10000)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .get()

            // 1. Identificar Emitente (Nome da Loja)
            val emitente = doc.select(".txtTopo, #emitente, .txtTit").firstOrNull()?.text()?.trim() ?: "Nota Fiscal"

            // 2. Extrair Chave de Acesso se disponível no HTML
            val rawHtml = doc.html()
            val chave = NfeUrlBuilder.extractChaveAcesso(rawHtml)

            // 3. Extrair Valor Total
            val totalText = doc.select(".totalNFe, .total, #totalValue").firstOrNull()?.text() ?: "0.0"
            val total = Regex("""\d+[.,]\d{2}""").find(totalText)?.value?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

            // 4. Extrair Itens (Padrão SEFAZ)
            val items = mutableListOf<ReceiptItem>()
            val rows = doc.select("table#tabResult tr") // Padrão da maioria dos estados

            for (row in rows) {
                val name = row.select(".txtTit").text().trim()
                val valStr = row.select(".valor").text().replace(",", ".").trim()
                val price = Regex("""\d+\.\d{2}""").find(valStr)?.value?.toDoubleOrNull() ?: 0.0
                val quantity = row.select(".Rqtd").text().split(":").lastOrNull()?.trim()?.toDoubleOrNull() ?: 1.0

                if (name.isNotEmpty() && price > 0) {
                    items.add(ReceiptItem(
                        descricao = name,
                        quantidade = quantity,
                        valorUnitario = price / quantity,
                        valorTotal = price
                    ))
                }
            }

            if (items.isEmpty()) return null // Indica que o scraper não conseguiu ler a estrutura

            Receipt(
                id = "scr-" + (chave ?: (emitente.hashCode().toString() + Clock.System.now().toEpochMilliseconds())),
                chaveAcesso = chave,
                emitente = emitente,
                valorTotal = if (total > 0) total else items.sumOf { it.valorTotal },
                categoria = "Geral",
                dataEmissao = "10/03/2026", // Idealmente extrair do HTML
                items = items,
                onlineUrl = url
            )
        } catch (e: Exception) {
            println("SCRAPER ERROR: ${e.message}")
            null
        }
    }
}

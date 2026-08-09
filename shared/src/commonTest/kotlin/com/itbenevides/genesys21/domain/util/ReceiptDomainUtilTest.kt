package com.itbenevides.genesys21.domain.util

import com.itbenevides.genesys21.domain.model.Receipt
import com.itbenevides.genesys21.domain.model.ReceiptItem
import com.itbenevides.genesys21.domain.service.ReceiptParserService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ReceiptDomainUtilTest {

    @Test
    fun testExtractChaveAcessoFromDanfe() {
        val sampleText = """
            RECEBEMOS DE KARZEN ELETRO OS PRODUTOS E/OU SERVIÇOS
            DANFE DOCUMENTO AUXILIAR DA NOTA FISCAL ELETRÔNICA
            CHAVE DE ACESSO
            3126 0322 1246 8700 0100 5501 2000 1565 8815 7587 6081
            VALOR TOTAL: R$ 158,49
        """.trimIndent()

        val extractedKey = NfeUrlBuilder.extractChaveAcesso(sampleText)
        assertEquals("31260322124687000100550120001565881575876081", extractedKey)
    }

    @Test
    fun testBuildOnlineUrlForMgNfe() {
        val chave = "31260322124687000100550120001565881575876081"
        val url = NfeUrlBuilder.buildOnlineUrl(chave)
        assertNotNull(url)
        assertTrue(url.contains("nfe.fazenda.mg.gov.br"))
        assertTrue(url.contains(chave))
    }

    @Test
    fun testBackupManagerExportAndImport() {
        val originalReceipts = listOf(
            Receipt(
                id = "1",
                chaveAcesso = "31260322124687000100550120001565881575876081",
                emitente = "KARZEN ELETRO",
                cnpjEmitente = "22.124.687/0001-00",
                dataEmissao = "10/03/2026",
                valorTotal = 158.49,
                categoria = "Eletrônicos",
                items = listOf(
                    ReceiptItem(
                        descricao = "Aparador de Pelos Mondial",
                        quantidade = 1.0,
                        valorUnitario = 158.49,
                        valorTotal = 158.49
                    )
                )
            )
        )

        val jsonExported = BackupManager.exportToJson(originalReceipts)
        assertTrue(jsonExported.contains("KARZEN ELETRO"))
        assertTrue(jsonExported.contains("158.49"))

        val importResult = BackupManager.importFromJson(jsonExported)
        assertTrue(importResult.isSuccess)
        val importedList = importResult.getOrThrow()
        assertEquals(1, importedList.size)
        assertEquals("KARZEN ELETRO", importedList[0].emitente)
        assertEquals(158.49, importedList[0].valorTotal)
        assertEquals(1, importedList[0].items.size)
    }

    @Test
    fun testLocalParserFiltersGarbageItems() {
        val parser = ReceiptParserService()
        val rawText = """
            COMPROVANTE FISCAL
            KARZEN ELETRO
            CPF: 123.456.789-00
            ITEM: Celular Galaxy S24 R$ 4.999,00
            VALOR ICMS R$ 899,00
            TOTAL R$ 4.999,00
            DISTRITO FEDERAL
        """.trimIndent()

        val receipt = parser.parseReceiptFromText(rawText)

        // Deve capturar o celular
        assertTrue(receipt.items.any { it.descricao.contains("GALAXY") })

        // Deve IGNORAR CPF, ICMS, TOTAL e DISTRITO
        assertTrue(receipt.items.none { it.descricao.contains("CPF") })
        assertTrue(receipt.items.none { it.descricao.contains("ICMS") })
        assertTrue(receipt.items.none { it.descricao.contains("TOTAL") })
        assertTrue(receipt.items.none { it.descricao.contains("DISTRITO") })

        assertEquals(1, receipt.items.size, "Deveria ter apenas 1 item (o celular)")
    }
}

package com.itbenevides.genesys21.domain.util

object NfeUrlBuilder {

    private val KEY_REGEX = Regex("""(?:\d[ \.\-]?){44}""")
    private val DIGITS_ONLY = Regex("""\D""")

    /**
     * Extrai a Chave de Acesso de 44 dígitos a partir de um texto bruto ou resultado de OCR.
     */
    fun extractChaveAcesso(rawText: String): String? {
        val matches = KEY_REGEX.findAll(rawText)
        for (match in matches) {
            val normalized = match.value.replace(DIGITS_ONLY, "")
            if (normalized.length == 44 && validateChaveDigits(normalized)) {
                return normalized
            }
        }
        return null
    }

    /**
     * Limpa e formata a chave de acesso em blocos de 4 dígitos (ex: 3126 0322 1246...)
     */
    fun formatChaveAcesso(chave: String): String {
        val clean = chave.replace(DIGITS_ONLY, "")
        if (clean.length != 44) return chave
        return clean.chunked(4).joinToString(" ")
    }

    /**
     * Gera a URL oficial de consulta pública da Nota Fiscal na SEFAZ / Portal da NF-e.
     */
    fun buildOnlineUrl(chave: String): String? {
        val cleanKey = chave.replace(DIGITS_ONLY, "")
        if (cleanKey.length != 44) return null

        val ufCode = cleanKey.substring(0, 2)
        val modelo = cleanKey.substring(20, 22) // 55 = NF-e, 65 = NFC-e

        return when {
            // Se for NFC-e (modelo 65), links estaduais diretos de consulta
            modelo == "65" -> getSefazNfceUrl(ufCode, cleanKey)
            // Portal Nacional da NF-e (modelo 55 ou fallback)
            else -> getSefazNfeUrl(cleanKey)
        }
    }

    private fun getSefazNfeUrl(cleanKey: String): String {
        // Portal Nacional centraliza todas as NF-e (Modelo 55) do Brasil e permite pré-preencher a chave.
        // O parâmetro 'nfe' preenche o campo no site, restando apenas resolver o Captcha.
        return "https://www.nfe.fazenda.gov.br/portal/consultaRecaptcha.aspx?tipoConsulta=resumo&nfe=$cleanKey"
    }

    private fun getSefazNfceUrl(ufCode: String, cleanKey: String): String {
        return when (ufCode) {
            "52" -> "https://nfe.sefaz.go.gov.br/nfeweb/sites/nfce/consulta-publica?chNFe=$cleanKey"
            "35" -> "https://www.nfce.fazenda.sp.gov.br/NFCeConsultaPublica/Paginas/ConsultaQRCode.aspx?p=$cleanKey"
            "31" -> "https://nfce.fazenda.mg.gov.br/portalnfce/sistema/consultaarrecadacao.xhtml?p=$cleanKey"
            "43" -> "https://www.sefaz.rs.gov.br/dfe/Consultas/ConsultaPublicaDfe?chNFe=$cleanKey"
            "33" -> "https://www.fazenda.rj.gov.br/nfce/consulta?p=$cleanKey"
            else -> "https://www.nfe.fazenda.gov.br/portal/consulta.aspx?chNFe=$cleanKey"
        }
    }

    private fun validateChaveDigits(key: String): Boolean {
        // Validação básica do tamanho e caracteres
        return key.length == 44 && key.all { it.isDigit() }
    }
}

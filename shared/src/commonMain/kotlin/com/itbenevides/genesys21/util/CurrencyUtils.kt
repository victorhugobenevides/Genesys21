package com.itbenevides.genesys21.util

import kotlin.math.roundToLong

object CurrencyUtils {

    /**
     * Arredonda um preço para exatamente 2 casas decimais.
     */
    fun roundPrice(value: Double): Double {
        return (value * 100.0).roundToLong() / 100.0
    }

    /**
     * Converte um valor monetário para centavos (padrão Stripe).
     * Garante que o arredondamento seja feito antes da conversão para evitar erros de ponto flutuante.
     */
    fun toStripeCents(value: Double): Long {
        return (value * 100.0).roundToLong()
    }

    /**
     * Formata um valor para exibição simples (ex: 123.45 -> "123.45").
     */
    fun formatDisplay(value: Double): String {
        return roundPrice(value).toString()
    }
}

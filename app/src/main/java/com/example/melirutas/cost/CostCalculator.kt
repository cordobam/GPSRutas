package com.example.melirutas.cost

/**
 * Costo de un viaje a partir del odómetro y el consumo del auto:
 *   km recorridos = km final − km inicial
 *   litros        = km recorridos ÷ rendimiento (km/l)
 *   costo total   = litros × precio por litro
 *
 * Kotlin puro (sin Android): testeable en tests JVM.
 */
object CostCalculator {

    /** Kilómetros recorridos. null si falta alguno de los dos o si el final es menor al inicial. */
    fun recorrido(kmInicial: Double?, kmFinal: Double?): Double? {
        if (kmInicial == null || kmFinal == null) return null
        val km = kmFinal - kmInicial
        return if (km >= 0.0) km else null
    }

    /** Litros a cargar. null si falta el recorrido o el rendimiento es 0 (o negativo). */
    fun litros(recorrido: Double?, kmPorLitro: Double?): Double? {
        if (recorrido == null || kmPorLitro == null || kmPorLitro <= 0.0) return null
        return recorrido / kmPorLitro
    }

    /** Costo total. null si falta la cantidad de litros o el precio por litro. */
    fun costo(litros: Double?, precioPorLitro: Double?): Double? {
        if (litros == null || precioPorLitro == null) return null
        return litros * precioPorLitro
    }

    /**
     * Convierte lo que el usuario escribió a número, aceptando coma o punto como
     * separador decimal ("1,5" y "1.5" dan 1.5) y puntos como separador de miles
     * ("1.234,56" da 1234.56).
     *
     * Devuelve null si el texto está vacío, no es numérico o es negativo.
     */
    fun parseDecimal(raw: String): Double? {
        val text = raw.trim()
            .replace(" ", "")
            .replace("$", "")
            .replace(",", ".")
        if (text.isEmpty()) return null

        val lastDot = text.lastIndexOf('.')
        val normalized = if (lastDot == -1) {
            text
        } else {
            val integerPart = text.substring(0, lastDot).replace(".", "")
            val decimals = text.substring(lastDot + 1).replace(".", "")
            "$integerPart.$decimals"
        }

        return normalized.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 }
    }

    /** Formatea un monto como "$ 1.234,56" (miles con punto, decimales con coma). */
    fun formatCurrency(value: Double): String {
        val negative = value < 0.0
        val cents = Math.round(Math.abs(value) * 100)

        val integerPart = cents / 100
        val decimals = (cents % 100).toString().padStart(2, '0')
        val grouped = integerPart.toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()

        return buildString {
            if (negative) append("-")
            append("$ ")
            append(grouped)
            append(",")
            append(decimals)
        }
    }

    /**
     * Formatea un número con miles separados por punto y coma decimal, sin ceros
     * finales (ej. "1.234", "12,5", "7,25"). Útil para km y litros.
     */
    fun formatNumber(value: Double, maxDecimals: Int): String {
        require(maxDecimals >= 0) { "maxDecimals no puede ser negativo" }

        val negative = value < 0.0
        val factor = Math.pow(10.0, maxDecimals.toDouble())
        val rounded = Math.round(Math.abs(value) * factor) / factor

        val integerPart = rounded.toLong()
        val grouped = integerPart.toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()

        val decimals = if (maxDecimals == 0) {
            ""
        } else {
            Math.round((rounded - integerPart) * factor)
                .toString()
                .padStart(maxDecimals, '0')
                .trimEnd('0')
        }

        return buildString {
            if (negative) append("-")
            append(grouped)
            if (decimals.isNotEmpty()) {
                append(",")
                append(decimals)
            }
        }
    }
}

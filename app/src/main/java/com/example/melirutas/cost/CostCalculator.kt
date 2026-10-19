package com.example.melirutas.cost

/**
 * Cuenta el costo de un viaje como km × precio por km, y formatea el resultado
 * en pesos argentinos ($ 1.234,56).
 *
 * Kotlin puro (sin Android): testeable en tests JVM.
 */
object CostCalculator {

    /** Costo total del viaje. */
    fun total(km: Double, pricePerKm: Double): Double = km * pricePerKm

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
}

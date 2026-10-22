package com.example.melirutas.cost

import java.util.Calendar

/**
 * Semanas calendario de lunes a domingo, para el consolidado de gastos de nafta.
 * Usa Calendar y no java.time porque minSdk 24 no trae java.time sin desugar.
 */
object Semana {

    /** Lunes 00:00 (hora local) de la semana que contiene a `millis`. */
    fun inicioDe(millis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        // DAY_OF_WEEK: domingo = 1, lunes = 2, ... sábado = 7.
        val diasDesdeLunes = (get(Calendar.DAY_OF_WEEK) + 5) % 7
        add(Calendar.DAY_OF_MONTH, -diasDesdeLunes)
    }.timeInMillis

    /** Suma (o resta, con número negativo) días corridos, respetando cambios de horario de verano. */
    fun masDias(millis: Long, dias: Int): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        add(Calendar.DAY_OF_MONTH, dias)
    }.timeInMillis

    /** true si `fecha` cae dentro de la semana que arranca en `inicioSemana` (lunes a domingo). */
    fun contiene(inicioSemana: Long, fecha: Long): Boolean =
        fecha >= inicioSemana && fecha < masDias(inicioSemana, 7)
}

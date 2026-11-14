package com.example.melirutas.codigos

import com.example.melirutas.data.db.DeliveryCodeEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ValidacionRango {
    data class Ok(val desde: Int, val hasta: Int) : ValidacionRango()
    data class Error(val mensaje: String) : ValidacionRango()
}

data class ResumenCodigos(
    val total: Int,
    val entregados: Int,
    val pendientes: Int,
    val ultimaHora: Int,
    val promedioPorHora: Double?
)

object CodigosLogic {

    const val MAX_CODIGO = 9999
    const val MAX_CODIGOS_POR_RANGO = 1000
    const val UNA_HORA_MILLIS = 60L * 60L * 1000L

    fun validarRango(desdeTexto: String, hastaTexto: String): ValidacionRango {
        val desde = desdeTexto.trim().toIntOrNull()
        val hasta = hastaTexto.trim().toIntOrNull()

        if (desde == null || hasta == null) {
            return ValidacionRango.Error("Completá desde y hasta con números.")
        }
        if (desde < 1 || hasta < 1) {
            return ValidacionRango.Error("Los códigos arrancan en 1.")
        }
        if (desde > hasta) {
            return ValidacionRango.Error("El código inicial no puede ser mayor que el final.")
        }
        if (hasta > MAX_CODIGO) {
            return ValidacionRango.Error("El código máximo permitido es $MAX_CODIGO.")
        }
        if (hasta - desde + 1 > MAX_CODIGOS_POR_RANGO) {
            return ValidacionRango.Error("El rango no puede superar los $MAX_CODIGOS_POR_RANGO códigos.")
        }

        return ValidacionRango.Ok(desde = desde, hasta = hasta)
    }

    fun faltantes(desde: Int, hasta: Int, existentes: Set<Int>): List<Int> =
        (desde..hasta).filterNot { it in existentes }

    fun resumen(
        codigos: List<DeliveryCodeEntity>,
        ahora: Long,
        esDiaEditable: Boolean
    ): ResumenCodigos {
        val entregados = codigos.mapNotNull { it.entregadoEn }
        val primera = entregados.minOrNull()
        val ultima = entregados.maxOrNull()
        val referencia = if (esDiaEditable) ahora else ultima

        val ultimaHora = if (referencia == null) {
            0
        } else {
            entregados.count { it > referencia - UNA_HORA_MILLIS && it <= referencia }
        }

        val promedio = if (primera == null || ultima == null) {
            null
        } else {
            val horas = (ultima - primera).toDouble() / UNA_HORA_MILLIS.toDouble()
            entregados.size / horas.coerceAtLeast(1.0)
        }

        return ResumenCodigos(
            total = codigos.size,
            entregados = entregados.size,
            pendientes = codigos.size - entregados.size,
            ultimaHora = ultimaHora,
            promedioPorHora = promedio
        )
    }

    fun fechaDia(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

    fun formatearDia(fecha: String): String = runCatching {
        val entrada = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha)
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(entrada!!)
    }.getOrDefault(fecha)

    fun formatearFechaHora(millis: Long): String =
        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(millis))
}

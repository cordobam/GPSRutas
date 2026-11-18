package com.example.melirutas.codigos

import com.example.melirutas.data.db.ResumenRutaEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ValidacionConteo {
    data class Ok(
        val paquetes: Int?,
        val paradas: Int?,
        val puntos: Int?
    ) : ValidacionConteo()

    data class Error(val mensaje: String) : ValidacionConteo()
}

data class ResumenRuta(
    val paquetes: Int?,
    val paradas: Int?,
    val puntos: Int?,
    val primera: Long?,
    val ultima: Long?,
    val duracionMillis: Long?,
    val paquetesPorHora: Double?
)

object RutaLogic {

    const val UNA_HORA_MILLIS = 60L * 60L * 1000L

    fun validarConteo(
        paquetesTexto: String,
        paradasTexto: String,
        puntosTexto: String
    ): ValidacionConteo {
        val (paquetes, errorPaquetes) = enteroOpcional(paquetesTexto, "Paquetes")
        if (errorPaquetes != null) return ValidacionConteo.Error(errorPaquetes)

        val (paradas, errorParadas) = enteroOpcional(paradasTexto, "Paradas")
        if (errorParadas != null) return ValidacionConteo.Error(errorParadas)

        val (puntos, errorPuntos) = enteroOpcional(puntosTexto, "Puntos")
        if (errorPuntos != null) return ValidacionConteo.Error(errorPuntos)

        return ValidacionConteo.Ok(
            paquetes = paquetes,
            paradas = paradas,
            puntos = puntos
        )
    }

    fun resumen(resumen: ResumenRutaEntity?): ResumenRuta {
        val paquetes = resumen?.paquetes
        val paradas = resumen?.paradas
        val puntos = resumen?.puntos
        val primera = resumen?.primerPedidoEn
        val ultima = resumen?.ultimoPedidoEn

        val duracion = if (primera != null && ultima != null && ultima >= primera) {
            ultima - primera
        } else {
            null
        }

        val porHora = if (paquetes == null || duracion == null) {
            null
        } else {
            val horas = duracion.toDouble() / UNA_HORA_MILLIS.toDouble()
            paquetes / horas.coerceAtLeast(1.0)
        }

        return ResumenRuta(
            paquetes = paquetes,
            paradas = paradas,
            puntos = puntos,
            primera = primera,
            ultima = ultima,
            duracionMillis = duracion,
            paquetesPorHora = porHora
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

    fun formatearHora(millis: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

    fun formatearDuracion(millis: Long): String {
        val minutos = millis / (60L * 1000L)
        val horas = minutos / 60L
        val resto = minutos % 60L
        return if (horas > 0L) "${horas}h ${resto}m" else "${minutos}m"
    }

    private fun enteroOpcional(texto: String, nombre: String): Pair<Int?, String?> {
        val limpio = texto.trim()
        if (limpio.isEmpty()) return null to null

        val valor = limpio.toIntOrNull() ?: return null to "$nombre debe ser un número entero."
        if (valor < 0) return null to "$nombre no puede ser negativo."

        return valor to null
    }
}

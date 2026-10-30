package com.example.melirutas.reparto

sealed class ResolucionTrack {
    data class Asignado(val zona: ZonaReparto, val esExcepcion: Boolean) : ResolucionTrack()
    data object SinAsignar : ResolucionTrack()
}

sealed class ValidacionTrack {
    data object Ok : ValidacionTrack()
    data class Excepcion(val zonaEnRango: ZonaReparto) : ValidacionTrack()
    data class Error(val mensaje: String) : ValidacionTrack()
}

object RepartoLogic {

    fun resolver(trackId: Long, zonas: List<ZonaReparto>): ResolucionTrack {
        val porExcepcion = zonas.firstOrNull { trackId in it.trackIds }
        if (porExcepcion != null) {
            return ResolucionTrack.Asignado(porExcepcion, esExcepcion = true)
        }
        val porRango = zonas.firstOrNull { zona ->
            zona.rangos.any { trackId >= it.desde && trackId <= it.hasta }
        }
        if (porRango != null) {
            return ResolucionTrack.Asignado(porRango, esExcepcion = false)
        }
        return ResolucionTrack.SinAsignar
    }

    fun validarNombreZona(
        nombre: String,
        zonas: List<ZonaReparto>,
        idEditando: String? = null
    ): String? {
        val limpio = nombre.trim()
        if (limpio.isEmpty()) return "Escribí un nombre para la zona."
        val repetida = zonas.any {
            it.id != idEditando && it.nombre.equals(limpio, ignoreCase = true)
        }
        return if (repetida) "Ya existe una zona con ese nombre." else null
    }

    fun validarRango(
        desde: Long,
        hasta: Long,
        zonas: List<ZonaReparto>
    ): String? {
        if (desde < 0 || hasta < 0) return "Los números no pueden ser negativos."
        if (desde > hasta) return "El inicio no puede ser mayor que el fin."

        val superpuestos = zonas.flatMap { zona ->
            zona.rangos
                .filter { existente -> desde <= existente.hasta && existente.desde <= hasta }
                .map { rango -> zona to rango }
        }
        if (superpuestos.isEmpty()) return null

        val detalle = superpuestos.joinToString("; ") { (zona, rango) ->
            "${rango.desde}–${rango.hasta} de \"${zona.nombre}\""
        }
        return "Se superpone con: $detalle."
    }

    fun validarTrack(
        trackId: Long,
        zonas: List<ZonaReparto>,
        zonaId: String
    ): ValidacionTrack {
        if (trackId < 0) return ValidacionTrack.Error("El track no puede ser negativo.")

        val repetido = zonas.firstOrNull { trackId in it.trackIds }
        if (repetido != null) {
            return ValidacionTrack.Error("El track $trackId ya está asignado a \"${repetido.nombre}\".")
        }

        val enRangoAjeno = zonas.firstOrNull { zona ->
            zona.id != zonaId && zona.rangos.any { trackId >= it.desde && trackId <= it.hasta }
        }
        if (enRangoAjeno != null) return ValidacionTrack.Excepcion(enRangoAjeno)

        return ValidacionTrack.Ok
    }

    fun nuevoDiaDesde(fecha: String, anterior: RepartoDia?): RepartoDia {
        if (anterior == null) return RepartoDia(fecha = fecha)
        val zonas = anterior.zonas.map { ZonaReparto(id = it.id, nombre = it.nombre) }
        return RepartoDia(fecha = fecha, zonas = zonas)
    }

    fun parseNumero(texto: String): Long? =
        texto.trim().toLongOrNull()?.takeIf { it >= 0 }
}

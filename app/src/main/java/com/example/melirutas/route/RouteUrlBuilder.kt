package com.example.melirutas.route

import java.net.URLEncoder

/**
 * Construye el deeplink de Google Maps para dirigir una ruta con varias paradas:
 * https://www.google.com/maps/dir/?api=1&destination=...&travelmode=driving&waypoints=...
 *
 * Reglas de Google Maps URLs (doc oficial):
 * - máximo 9 waypoints: con destino final eso cubre hasta 10 paradas.
 * - la URL completa no puede superar 2048 caracteres.
 * - si no se pasa "origin", Google usa la ubicación actual del dispositivo.
 * - los waypoints se respetan en el orden en que se listan.
 *
 * Kotlin puro (sin Android): testeable en tests JVM.
 */
object RouteUrlBuilder {

    const val MAX_STOPS = 10
    const val MAX_URL_LENGTH = 2048
    private const val BASE_URL = "https://www.google.com/maps/dir/?api=1"

    /** Parada de la ruta: lo que el usuario dejó en la pantalla de revisión. */
    data class RouteStop(
        val street: String,
        val city: String = "",
        val postalCode: String? = null
    )

    sealed class Result {
        data class Success(val url: String) : Result()
        data class Error(val reason: Reason) : Result()

        enum class Reason {
            NO_STOPS,
            TOO_MANY_STOPS,
            URL_TOO_LONG
        }
    }

    fun build(stops: List<RouteStop>): Result {
        if (stops.isEmpty()) {
            return Result.Error(Result.Reason.NO_STOPS)
        }
        if (stops.size > MAX_STOPS) {
            return Result.Error(Result.Reason.TOO_MANY_STOPS)
        }

        buildWith(stops, includeLocality = true)?.let { return Result.Success(it) }
        buildWith(stops, includeLocality = false)?.let { return Result.Success(it) }
        return Result.Error(Result.Reason.URL_TOO_LONG)
    }

    /**
     * Arma la URL. Con includeLocality cada parada es "calle, localidad CP, Argentina";
     * sin ella solo "calle, Argentina" (recorte de emergencia para respetar los 2048 chars).
     * Devuelve null si igual así se pasa del límite.
     */
    private fun buildWith(stops: List<RouteStop>, includeLocality: Boolean): String? {
        val queries = stops.map { it.toQuery(includeLocality) }

        val url = StringBuilder(BASE_URL)
            .append("&destination=").append(encode(queries.last()))
            .append("&travelmode=driving")
        if (queries.size > 1) {
            url.append("&waypoints=")
                .append(queries.dropLast(1).joinToString("%7C") { encode(it) })
        }

        val result = url.toString()
        return if (result.length <= MAX_URL_LENGTH) result else null
    }

    private fun RouteStop.toQuery(includeLocality: Boolean): String {
        val locality = if (includeLocality) {
            listOfNotNull(
                city.trim().ifBlank { null },
                postalCode?.trim()?.ifBlank { null }
            ).joinToString(" ")
        } else {
            ""
        }
        return listOfNotNull(
            street.trim().ifBlank { null },
            locality.ifBlank { null },
            "Argentina"
        ).joinToString(", ")
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}

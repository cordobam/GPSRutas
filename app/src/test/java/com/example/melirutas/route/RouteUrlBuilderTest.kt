package com.example.melirutas.route

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLDecoder

class RouteUrlBuilderTest {

    private fun stop(n: Int) = RouteUrlBuilder.RouteStop(
        street = "Calle $n Piso $n",
        city = "Córdoba",
        postalCode = "500$n"
    )

    private fun decode(url: String): String = URLDecoder.decode(url, "UTF-8")

    @Test
    fun ceroParadas_error() {
        val result = RouteUrlBuilder.build(emptyList())
        assertEquals(
            RouteUrlBuilder.Result.Error(RouteUrlBuilder.Result.Reason.NO_STOPS),
            result
        )
    }

    @Test
    fun onceParadas_error() {
        val result = RouteUrlBuilder.build((1..11).map { stop(it) })
        assertEquals(
            RouteUrlBuilder.Result.Error(RouteUrlBuilder.Result.Reason.TOO_MANY_STOPS),
            result
        )
    }

    @Test
    fun unaParada_soloDestinationSinWaypoints() {
        val result = RouteUrlBuilder.build(listOf(stop(1)))
        assertTrue(result is RouteUrlBuilder.Result.Success)
        val url = (result as RouteUrlBuilder.Result.Success).url

        assertTrue(url.startsWith("https://www.google.com/maps/dir/?api=1"))
        assertTrue(url.contains("&travelmode=driving"))
        assertFalse(url.contains("waypoints"))
        assertTrue(url.contains("destination=Calle%201%20Piso%201%2C%20C%C3%B3rdoba%205001%2C%20Argentina"))
    }

    @Test
    fun nueveParadas_ordenDeWaypointsYDestinoFinal() {
        val stops = (1..9).map { stop(it) }
        val result = RouteUrlBuilder.build(stops)
        assertTrue(result is RouteUrlBuilder.Result.Success)
        val url = (result as RouteUrlBuilder.Result.Success).url
        val decoded = decode(url)

        // destino = parada 9 (la última)
        assertTrue(decoded.contains("destination=Calle 9 Piso 9, Córdoba 5009, Argentina"))
        // waypoints = paradas 1..8 en orden, separados por pipe
        val waypoints = decoded.substringAfter("waypoints=").substringBefore("&")
        val expected = (1..8).joinToString("|") { "Calle $it Piso $it, Córdoba 500$it, Argentina" }
        assertEquals(expected, waypoints)
        // 8 waypoints es el máximo permitido por Google (9 es el tope documentado)
        assertEquals(8, waypoints.split("|").size)
    }

    @Test
    fun diezParadas_esElMaximoSoportado() {
        val result = RouteUrlBuilder.build((1..10).map { stop(it) })
        assertTrue(result is RouteUrlBuilder.Result.Success)
    }

    @Test
    fun espaciosYAcentos_codificados() {
        val stop = RouteUrlBuilder.RouteStop("Calle Ñandú 742", "Córdoba", "5000")
        val result = RouteUrlBuilder.build(listOf(stop))
        val url = (result as RouteUrlBuilder.Result.Success).url

        assertFalse(url.contains(" "))
        assertFalse(url.contains("+"))
        assertTrue(url.contains("%20"))
        assertTrue(decode(url).contains("Calle Ñandú 742, Córdoba 5000, Argentina"))
    }

    @Test
    fun ciudadSinCp_oCpSinCiudad() {
        val conCiudad = RouteUrlBuilder.build(
            listOf(RouteUrlBuilder.RouteStop("Calle X 1", "Córdoba", null))
        ) as RouteUrlBuilder.Result.Success
        assertTrue(decode(conCiudad.url).contains("Calle X 1, Córdoba, Argentina"))

        val soloCp = RouteUrlBuilder.build(
            listOf(RouteUrlBuilder.RouteStop("Calle X 1", "", "5000"))
        ) as RouteUrlBuilder.Result.Success
        assertTrue(decode(soloCp.url).contains("Calle X 1, 5000, Argentina"))
    }

    @Test
    fun urlLarga_seRecortaASoloCalle() {
        // Con locality la URL queda 2071 chars (>2048); sin locality 1871 (≤2048).
        val stops = (1..8).map {
            RouteUrlBuilder.RouteStop(street = "a".repeat(202) + " $it", city = "Córdoba", postalCode = "5001")
        }

        val result = RouteUrlBuilder.build(stops)
        assertTrue(result is RouteUrlBuilder.Result.Success)
        val url = (result as RouteUrlBuilder.Result.Success).url

        assertTrue(url.length <= RouteUrlBuilder.MAX_URL_LENGTH)
        assertFalse(decode(url).contains("Córdoba"))
        assertTrue(decode(url).contains("Argentina"))
    }

    @Test
    fun urlExtremaImposible_hastaSoloCalle_noCabe() {
        val street = "X".repeat(300)
        val stops = (1..10).map { RouteUrlBuilder.RouteStop(street = "$street $it") }

        val result = RouteUrlBuilder.build(stops)
        assertEquals(
            RouteUrlBuilder.Result.Error(RouteUrlBuilder.Result.Reason.URL_TOO_LONG),
            result
        )
    }
}

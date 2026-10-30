package com.example.melirutas.reparto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RepartoLogicTest {

    private fun zona(
        id: String,
        nombre: String,
        rangos: List<ReglaRango> = emptyList(),
        trackIds: List<Long> = emptyList()
    ) = ZonaReparto(id = id, nombre = nombre, rangos = rangos, trackIds = trackIds)

    @Test
    fun resolver_trackPuntualGanaSobreRangoDeOtraZona() {
        val adelante = zona("adelante", "Adelante", rangos = listOf(ReglaRango(1, 10)))
        val baul = zona("baul", "Baúl", trackIds = listOf(5))

        val resultado = RepartoLogic.resolver(5, listOf(adelante, baul))

        assertTrue(resultado is ResolucionTrack.Asignado)
        resultado as ResolucionTrack.Asignado
        assertEquals("Baúl", resultado.zona.nombre)
        assertTrue(resultado.esExcepcion)
    }

    @Test
    fun resolver_rangoInclusivoEnBordes() {
        val zonas = listOf(zona("a", "Atrás derecha", rangos = listOf(ReglaRango(1, 10))))

        listOf(1L, 10L).forEach { trackId ->
            val resultado = RepartoLogic.resolver(trackId, zonas)
            assertTrue(resultado is ResolucionTrack.Asignado)
            resultado as ResolucionTrack.Asignado
            assertEquals("Atrás derecha", resultado.zona.nombre)
            assertEquals(false, resultado.esExcepcion)
        }
    }

    @Test
    fun resolver_numeroFueraDeRango_sinAsignar() {
        val zonas = listOf(zona("a", "Atrás derecha", rangos = listOf(ReglaRango(1, 10))))

        assertTrue(RepartoLogic.resolver(11, zonas) is ResolucionTrack.SinAsignar)
        assertTrue(RepartoLogic.resolver(0, zonas) is ResolucionTrack.SinAsignar)
    }

    @Test
    fun resolver_sinZonas_sinAsignar() {
        assertTrue(RepartoLogic.resolver(1, emptyList()) is ResolucionTrack.SinAsignar)
    }

    @Test
    fun validarRango_adyacenteEsValido() {
        val zonas = listOf(zona("a", "A", rangos = listOf(ReglaRango(1, 10))))

        assertNull(RepartoLogic.validarRango(11, 20, zonas))
    }

    @Test
    fun validarRango_inicioMayorQueFin_error() {
        assertNotNull(RepartoLogic.validarRango(20, 10, emptyList()))
    }

    @Test
    fun validarRango_negativo_error() {
        assertNotNull(RepartoLogic.validarRango(-1, 10, emptyList()))
        assertNotNull(RepartoLogic.validarRango(1, -10, emptyList()))
    }

    @Test
    fun validarRango_solapamientoMismaZona_error() {
        val zonas = listOf(zona("a", "A", rangos = listOf(ReglaRango(1, 10))))

        assertNotNull(RepartoLogic.validarRango(5, 15, zonas))
    }

    @Test
    fun validarRango_solapamientoOtraZona_errorConNombre() {
        val zonas = listOf(
            zona("a", "Atrás derecha", rangos = listOf(ReglaRango(1, 10))),
            zona("b", "Baúl", rangos = listOf(ReglaRango(30, 40)))
        )

        val error = RepartoLogic.validarRango(5, 35, zonas)

        assertNotNull(error)
        assertTrue(error!!.contains("Atrás derecha"))
        assertTrue(error.contains("Baúl"))
    }

    @Test
    fun validarTrack_ok() {
        val zonas = listOf(zona("a", "A"))

        val resultado = RepartoLogic.validarTrack(123, zonas, "a")

        assertEquals(ValidacionTrack.Ok, resultado)
    }

    @Test
    fun validarTrack_duplicadoEnOtraZona_error() {
        val zonas = listOf(
            zona("a", "Adelante"),
            zona("b", "Baúl", trackIds = listOf(17))
        )

        val resultado = RepartoLogic.validarTrack(17, zonas, "a")

        assertTrue(resultado is ValidacionTrack.Error)
        resultado as ValidacionTrack.Error
        assertTrue(resultado.mensaje.contains("Baúl"))
    }

    @Test
    fun validarTrack_duplicadoEnMismaZona_error() {
        val zonas = listOf(zona("a", "Adelante", trackIds = listOf(17)))

        assertTrue(RepartoLogic.validarTrack(17, zonas, "a") is ValidacionTrack.Error)
    }

    @Test
    fun validarTrack_dentroDeRangoAjeno_esExcepcion() {
        val zonas = listOf(
            zona("a", "Adelante", rangos = listOf(ReglaRango(1, 10))),
            zona("b", "Baúl")
        )

        val resultado = RepartoLogic.validarTrack(5, zonas, "b")

        assertTrue(resultado is ValidacionTrack.Excepcion)
        resultado as ValidacionTrack.Excepcion
        assertEquals("Adelante", resultado.zonaEnRango.nombre)
    }

    @Test
    fun validarTrack_dentroDeRangoPropio_esOk() {
        val zonas = listOf(zona("a", "Adelante", rangos = listOf(ReglaRango(1, 10))))

        assertEquals(ValidacionTrack.Ok, RepartoLogic.validarTrack(5, zonas, "a"))
    }

    @Test
    fun validarTrack_negativo_error() {
        assertTrue(RepartoLogic.validarTrack(-1, emptyList(), "a") is ValidacionTrack.Error)
    }

    @Test
    fun validarNombreZona_vacio_error() {
        assertNotNull(RepartoLogic.validarNombreZona("   ", emptyList()))
    }

    @Test
    fun validarNombreZona_duplicadoIgnoraMayusculas_error() {
        val zonas = listOf(zona("a", "Baúl"))

        assertNotNull(RepartoLogic.validarNombreZona("baúl", zonas))
    }

    @Test
    fun validarNombreZona_mismoIdPermitido() {
        val zonas = listOf(zona("a", "Baúl"))

        assertNull(RepartoLogic.validarNombreZona("Baúl", zonas, idEditando = "a"))
    }

    @Test
    fun nuevoDiaDesde_copiaZonasSinReglas() {
        val anterior = RepartoDia(
            fecha = "2026-10-29",
            zonas = listOf(
                zona(
                    "a",
                    "Atrás derecha",
                    rangos = listOf(ReglaRango(1, 10)),
                    trackIds = listOf(5)
                )
            )
        )

        val nuevo = RepartoLogic.nuevoDiaDesde("2026-10-30", anterior)

        assertEquals("2026-10-30", nuevo.fecha)
        assertEquals(1, nuevo.zonas.size)
        assertEquals("a", nuevo.zonas[0].id)
        assertEquals("Atrás derecha", nuevo.zonas[0].nombre)
        assertTrue(nuevo.zonas[0].rangos.isEmpty())
        assertTrue(nuevo.zonas[0].trackIds.isEmpty())
    }

    @Test
    fun nuevoDiaDesde_sinAnterior_quedaVacio() {
        val nuevo = RepartoLogic.nuevoDiaDesde("2026-10-30", null)

        assertEquals("2026-10-30", nuevo.fecha)
        assertTrue(nuevo.zonas.isEmpty())
    }

    @Test
    fun parseNumero_casos() {
        assertEquals(10L, RepartoLogic.parseNumero("10"))
        assertEquals(15L, RepartoLogic.parseNumero("  15 "))
        assertEquals(0L, RepartoLogic.parseNumero("0"))
        assertNull(RepartoLogic.parseNumero(""))
        assertNull(RepartoLogic.parseNumero("abc"))
        assertNull(RepartoLogic.parseNumero("-5"))
        assertNull(RepartoLogic.parseNumero("999999999999999999999"))
    }
}

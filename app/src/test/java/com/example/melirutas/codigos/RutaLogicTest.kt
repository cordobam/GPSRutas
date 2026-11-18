package com.example.melirutas.codigos

import com.example.melirutas.data.db.ResumenRutaEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RutaLogicTest {

    private val hora = RutaLogic.UNA_HORA_MILLIS

    private fun resumen(
        paquetes: Int? = null,
        paradas: Int? = null,
        puntos: Int? = null,
        primera: Long? = null,
        ultima: Long? = null
    ) = ResumenRutaEntity(
        fecha = "2026-11-18",
        paquetes = paquetes,
        paradas = paradas,
        puntos = puntos,
        primerPedidoEn = primera,
        ultimoPedidoEn = ultima
    )

    @Test
    fun validarConteo_vacio_acepta() {
        val resultado = RutaLogic.validarConteo("", "", "")

        assertTrue(resultado is ValidacionConteo.Ok)
        resultado as ValidacionConteo.Ok
        assertNull(resultado.paquetes)
        assertNull(resultado.paradas)
        assertNull(resultado.puntos)
    }

    @Test
    fun validarConteo_valores_ok() {
        val resultado = RutaLogic.validarConteo("12", "5", "30")

        assertTrue(resultado is ValidacionConteo.Ok)
        resultado as ValidacionConteo.Ok
        assertEquals(12, resultado.paquetes)
        assertEquals(5, resultado.paradas)
        assertEquals(30, resultado.puntos)
    }

    @Test
    fun validarConteo_paquetesNoNumerico_error() {
        val resultado = RutaLogic.validarConteo("abc", "5", "30")

        assertTrue(resultado is ValidacionConteo.Error)
        assertTrue((resultado as ValidacionConteo.Error).mensaje.contains("Paquetes"))
    }

    @Test
    fun validarConteo_paradasNegativas_error() {
        val resultado = RutaLogic.validarConteo("12", "-1", "30")

        assertTrue(resultado is ValidacionConteo.Error)
        assertTrue((resultado as ValidacionConteo.Error).mensaje.contains("Paradas"))
    }

    @Test
    fun validarConteo_puntosNoNumerico_error() {
        val resultado = RutaLogic.validarConteo("12", "5", "x")

        assertTrue(resultado is ValidacionConteo.Error)
        assertTrue((resultado as ValidacionConteo.Error).mensaje.contains("Puntos"))
    }

    @Test
    fun resumen_sinDatos() {
        val resultado = RutaLogic.resumen(null)

        assertNull(resultado.paquetes)
        assertNull(resultado.paradas)
        assertNull(resultado.puntos)
        assertNull(resultado.primera)
        assertNull(resultado.ultima)
        assertNull(resultado.duracionMillis)
        assertNull(resultado.paquetesPorHora)
    }

    @Test
    fun resumen_soloPaquetes_sinPromedio() {
        val resultado = RutaLogic.resumen(resumen(paquetes = 10))

        assertEquals(10, resultado.paquetes)
        assertNull(resultado.paquetesPorHora)
    }

    @Test
    fun resumen_menosDeUnaHora_usaMinimoUnaHora() {
        val primera = 10 * hora
        val resultado = RutaLogic.resumen(
            resumen(
                paquetes = 30,
                primera = primera,
                ultima = primera + 30L * 60L * 1000L
            )
        )

        assertEquals(30L * 60L * 1000L, resultado.duracionMillis)
        assertEquals(30.0, resultado.paquetesPorHora!!, 0.001)
    }

    @Test
    fun resumen_masDeUnaHora() {
        val primera = 10 * hora
        val resultado = RutaLogic.resumen(
            resumen(
                paquetes = 100,
                primera = primera,
                ultima = primera + 2 * hora
            )
        )

        assertEquals(2 * hora, resultado.duracionMillis)
        assertEquals(50.0, resultado.paquetesPorHora!!, 0.001)
    }

    @Test
    fun resumen_ceroPaquetes() {
        val primera = 10 * hora
        val resultado = RutaLogic.resumen(
            resumen(
                paquetes = 0,
                primera = primera,
                ultima = primera + hora
            )
        )

        assertEquals(0.0, resultado.paquetesPorHora!!, 0.001)
    }

    @Test
    fun resumen_horariosInvertidos() {
        val resultado = RutaLogic.resumen(
            resumen(
                paquetes = 10,
                primera = 2 * hora,
                ultima = hora
            )
        )

        assertNull(resultado.duracionMillis)
        assertNull(resultado.paquetesPorHora)
    }

    @Test
    fun resumen_conservaParadasYPuntos() {
        val resultado = RutaLogic.resumen(
            resumen(
                paquetes = 10,
                paradas = 8,
                puntos = 25,
                primera = 10 * hora,
                ultima = 11 * hora
            )
        )

        assertEquals(8, resultado.paradas)
        assertEquals(25, resultado.puntos)
        assertEquals(10.0, resultado.paquetesPorHora!!, 0.001)
    }
}

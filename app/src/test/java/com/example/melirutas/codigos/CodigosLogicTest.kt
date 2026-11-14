package com.example.melirutas.codigos

import com.example.melirutas.data.db.DeliveryCodeEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CodigosLogicTest {

    private val hora = CodigosLogic.UNA_HORA_MILLIS

    private fun codigo(
        numero: Int,
        entregadoEn: Long? = null,
        fecha: String = "2026-11-14"
    ) = DeliveryCodeEntity(
        fecha = fecha,
        codigo = numero,
        entregadoEn = entregadoEn
    )

    @Test
    fun validarRango_ok() {
        val resultado = CodigosLogic.validarRango("1", "100")

        assertTrue(resultado is ValidacionRango.Ok)
        resultado as ValidacionRango.Ok
        assertEquals(1, resultado.desde)
        assertEquals(100, resultado.hasta)
    }

    @Test
    fun validarRango_bordesPermitidos() {
        assertTrue(CodigosLogic.validarRango("1", "1000") is ValidacionRango.Ok)
        assertTrue(CodigosLogic.validarRango("9999", "9999") is ValidacionRango.Ok)
    }

    @Test
    fun validarRango_vacio_error() {
        assertTrue(CodigosLogic.validarRango("", "") is ValidacionRango.Error)
        assertTrue(CodigosLogic.validarRango("1", "") is ValidacionRango.Error)
    }

    @Test
    fun validarRango_noNumerico_error() {
        assertTrue(CodigosLogic.validarRango("abc", "100") is ValidacionRango.Error)
    }

    @Test
    fun validarRango_cero_error() {
        assertTrue(CodigosLogic.validarRango("0", "100") is ValidacionRango.Error)
    }

    @Test
    fun validarRango_invertido_error() {
        assertTrue(CodigosLogic.validarRango("100", "1") is ValidacionRango.Error)
    }

    @Test
    fun validarRango_maximo_error() {
        assertTrue(CodigosLogic.validarRango("1", "10000") is ValidacionRango.Error)
    }

    @Test
    fun validarRango_demasiadoGrande_error() {
        assertTrue(CodigosLogic.validarRango("1", "2000") is ValidacionRango.Error)
    }

    @Test
    fun faltantes_sinExistentes_devuelveTodos() {
        assertEquals(
            listOf(1, 2, 3, 4, 5),
            CodigosLogic.faltantes(1, 5, emptySet())
        )
    }

    @Test
    fun faltantes_conExistentes_devuelveSoloFaltantes() {
        assertEquals(
            listOf(1, 3, 5),
            CodigosLogic.faltantes(1, 5, setOf(2, 4))
        )
    }

    @Test
    fun faltantes_todosExistentes_vacio() {
        assertTrue(CodigosLogic.faltantes(1, 3, setOf(1, 2, 3)).isEmpty())
    }

    @Test
    fun faltantes_respetaBordes() {
        assertEquals(
            listOf(2, 3, 4),
            CodigosLogic.faltantes(1, 5, setOf(1, 5))
        )
    }

    @Test
    fun resumen_vacio() {
        val resumen = CodigosLogic.resumen(emptyList(), ahora = 10 * hora, esDiaEditable = true)

        assertEquals(0, resumen.total)
        assertEquals(0, resumen.entregados)
        assertEquals(0, resumen.pendientes)
        assertEquals(0, resumen.ultimaHora)
        assertNull(resumen.promedioPorHora)
    }

    @Test
    fun resumen_sinEntregas() {
        val resumen = CodigosLogic.resumen(
            listOf(codigo(1), codigo(2)),
            ahora = 10 * hora,
            esDiaEditable = true
        )

        assertEquals(2, resumen.total)
        assertEquals(0, resumen.entregados)
        assertEquals(2, resumen.pendientes)
        assertEquals(0, resumen.ultimaHora)
        assertNull(resumen.promedioPorHora)
    }

    @Test
    fun resumen_diaEditable_ultimaHora() {
        val ahora = 10 * hora
        val resumen = CodigosLogic.resumen(
            listOf(
                codigo(1, ahora - 10 * 60 * 1000),
                codigo(2, ahora - 2 * hora),
                codigo(3)
            ),
            ahora = ahora,
            esDiaEditable = true
        )

        assertEquals(3, resumen.total)
        assertEquals(2, resumen.entregados)
        assertEquals(1, resumen.pendientes)
        assertEquals(1, resumen.ultimaHora)
        assertEquals(2.0 / (110.0 / 60.0), resumen.promedioPorHora!!, 0.001)
    }

    @Test
    fun resumen_historico_usaUltimaEntregaComoReferencia() {
        val ultima = 5 * hora
        val resumen = CodigosLogic.resumen(
            listOf(
                codigo(1, ultima - 2 * hora),
                codigo(2, ultima - 30 * 60 * 1000),
                codigo(3, ultima)
            ),
            ahora = 100 * hora,
            esDiaEditable = false
        )

        assertEquals(3, resumen.total)
        assertEquals(3, resumen.entregados)
        assertEquals(0, resumen.pendientes)
        assertEquals(2, resumen.ultimaHora)
        assertEquals(1.5, resumen.promedioPorHora!!, 0.001)
    }

    @Test
    fun resumen_promedioMinimoUnaHora() {
        val primera = 10 * hora
        val resumen = CodigosLogic.resumen(
            listOf(
                codigo(1, primera),
                codigo(2, primera + 10 * 60 * 1000)
            ),
            ahora = primera + 15 * 60 * 1000,
            esDiaEditable = true
        )

        assertEquals(2, resumen.ultimaHora)
        assertEquals(2.0, resumen.promedioPorHora!!, 0.001)
    }
}

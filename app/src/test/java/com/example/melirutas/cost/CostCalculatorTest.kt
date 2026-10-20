package com.example.melirutas.cost

import org.junit.Assert.assertEquals
import org.junit.Test

class CostCalculatorTest {

    @Test
    fun recorrido_normal() {
        assertEquals(350.0, CostCalculator.recorrido(1000.0, 1350.0)!!, 0.001)
    }

    @Test
    fun recorrido_conDecimales() {
        assertEquals(412.5, CostCalculator.recorrido(12000.0, 12412.5)!!, 0.001)
    }

    @Test
    fun recorrido_cero() {
        assertEquals(0.0, CostCalculator.recorrido(1000.0, 1000.0)!!, 0.001)
    }

    @Test
    fun recorrido_finalMenor_null() {
        assertEquals(null, CostCalculator.recorrido(1500.0, 1000.0))
    }

    @Test
    fun recorrido_faltaAlguno_null() {
        assertEquals(null, CostCalculator.recorrido(null, 1000.0))
        assertEquals(null, CostCalculator.recorrido(1000.0, null))
        assertEquals(null, CostCalculator.recorrido(null, null))
    }

    @Test
    fun litros_normal() {
        assertEquals(35.0, CostCalculator.litros(350.0, 10.0)!!, 0.001)
    }

    @Test
    fun litros_conDecimales() {
        assertEquals(41.25, CostCalculator.litros(412.5, 10.0)!!, 0.001)
    }

    @Test
    fun litros_rendimientoCero_null() {
        assertEquals(null, CostCalculator.litros(350.0, 0.0))
    }

    @Test
    fun litros_sinRecorrido_null() {
        assertEquals(null, CostCalculator.litros(null, 10.0))
    }

    @Test
    fun costo_normal() {
        assertEquals(52500.0, CostCalculator.costo(35.0, 1500.0)!!, 0.001)
    }

    @Test
    fun costo_faltaDato_null() {
        assertEquals(null, CostCalculator.costo(null, 1500.0))
        assertEquals(null, CostCalculator.costo(35.0, null))
    }

    @Test
    fun viajeCompleto_encadenado() {
        val km = CostCalculator.recorrido(1000.0, 1350.0)
        val litros = CostCalculator.litros(km, 10.0)
        val total = CostCalculator.costo(litros, 1500.0)
        assertEquals(52500.0, total!!, 0.001)
    }

    @Test
    fun parseDecimal_conComa() {
        assertEquals(1.5, CostCalculator.parseDecimal("1,5")!!, 0.0001)
    }

    @Test
    fun parseDecimal_conPunto() {
        assertEquals(12.5, CostCalculator.parseDecimal("12.5")!!, 0.0001)
    }

    @Test
    fun parseDecimal_milesYDecimales() {
        assertEquals(1234.56, CostCalculator.parseDecimal("1.234,56")!!, 0.0001)
    }

    @Test
    fun parseDecimal_enteroConEspaciosYSimbolo() {
        assertEquals(350.0, CostCalculator.parseDecimal(" $ 350 ")!!, 0.0001)
    }

    @Test
    fun parseDecimal_vacioONoNumerico_null() {
        assertEquals(null, CostCalculator.parseDecimal(""))
        assertEquals(null, CostCalculator.parseDecimal("   "))
        assertEquals(null, CostCalculator.parseDecimal("abc"))
    }

    @Test
    fun parseDecimal_negativo_null() {
        assertEquals(null, CostCalculator.parseDecimal("-5"))
    }

    @Test
    fun formatCurrency_basico() {
        assertEquals("$ 4.375,00", CostCalculator.formatCurrency(4375.0))
    }

    @Test
    fun formatCurrency_conCentavos() {
        assertEquals("$ 1.234,50", CostCalculator.formatCurrency(1234.5))
    }

    @Test
    fun formatCurrency_cero() {
        assertEquals("$ 0,00", CostCalculator.formatCurrency(0.0))
    }

    @Test
    fun formatCurrency_millonesRedondea() {
        assertEquals("$ 1.234.567,89", CostCalculator.formatCurrency(1234567.891))
    }

    @Test
    fun formatNumber_entero() {
        assertEquals("350", CostCalculator.formatNumber(350.0, maxDecimals = 1))
    }

    @Test
    fun formatNumber_unDecimal() {
        assertEquals("12,5", CostCalculator.formatNumber(12.5, maxDecimals = 2))
    }

    @Test
    fun formatNumber_miles() {
        assertEquals("1.234", CostCalculator.formatNumber(1234.0, maxDecimals = 2))
    }

    @Test
    fun formatNumber_dosDecimales() {
        assertEquals("7,25", CostCalculator.formatNumber(7.25, maxDecimals = 2))
    }

    @Test
    fun formatNumber_redondea() {
        assertEquals("12,57", CostCalculator.formatNumber(12.567, maxDecimals = 2))
    }

    @Test
    fun formatNumber_sinDecimales() {
        assertEquals("12", CostCalculator.formatNumber(12.4, maxDecimals = 0))
    }
}

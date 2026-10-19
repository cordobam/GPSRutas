package com.example.melirutas.cost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CostCalculatorTest {

    @Test
    fun total_kmPorPrecio() {
        assertEquals(3500.0, CostCalculator.total(km = 10.0, pricePerKm = 350.0), 0.001)
    }

    @Test
    fun total_conDecimales() {
        assertEquals(4375.0, CostCalculator.total(km = 12.5, pricePerKm = 350.0), 0.001)
    }

    @Test
    fun total_cero() {
        assertEquals(0.0, CostCalculator.total(km = 0.0, pricePerKm = 500.0), 0.001)
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
}

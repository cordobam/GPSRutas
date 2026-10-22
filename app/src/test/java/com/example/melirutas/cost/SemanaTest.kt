package com.example.melirutas.cost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SemanaTest {

    private fun fecha(anio: Int, mes: Int, dia: Int, hora: Int = 12): Long =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, anio)
            set(Calendar.MONTH, mes - 1)
            set(Calendar.DAY_OF_MONTH, dia)
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    @Test
    fun inicioDe_miercoles_vuelveAlLunesDeEsaSemana() {
        // Miércoles 21/10/2026 → lunes 19/10/2026.
        assertEquals(fecha(2026, 10, 19, 0), Semana.inicioDe(fecha(2026, 10, 21, 15)))
    }

    @Test
    fun inicioDe_domingo_vuelveAlLunesAnterior() {
        // Domingo 18/10/2026 es el último día de la semana 12–18 → lunes 12/10/2026.
        assertEquals(fecha(2026, 10, 12, 0), Semana.inicioDe(fecha(2026, 10, 18, 9)))
    }

    @Test
    fun inicioDe_lunes_noSeCorre() {
        assertEquals(fecha(2026, 10, 19, 0), Semana.inicioDe(fecha(2026, 10, 19, 0)))
    }

    @Test
    fun masDias_sieteDias_pasaALaProximaSemana() {
        val inicio = Semana.inicioDe(fecha(2026, 10, 21, 15))
        assertEquals(fecha(2026, 10, 26, 0), Semana.masDias(inicio, 7))
    }

    @Test
    fun contiene_aceptaLunesYDomingo() {
        val inicio = Semana.inicioDe(fecha(2026, 10, 21, 15))
        assertTrue(Semana.contiene(inicio, fecha(2026, 10, 19, 0)))
        assertTrue(Semana.contiene(inicio, fecha(2026, 10, 25, 23)))
        assertFalse(Semana.contiene(inicio, fecha(2026, 10, 18, 23)))
        assertFalse(Semana.contiene(inicio, fecha(2026, 10, 26, 0)))
    }

    @Test
    fun inicioDe_semanasDistintas_noSeSolapan() {
        val esta = Semana.inicioDe(fecha(2026, 10, 21, 15))
        val anterior = Semana.inicioDe(Semana.masDias(esta, -7))
        assertEquals(Semana.masDias(anterior, 7), esta)
        assertFalse(Semana.contiene(anterior, fecha(2026, 10, 19, 12)))
    }
}

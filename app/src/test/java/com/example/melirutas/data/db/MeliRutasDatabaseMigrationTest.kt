package com.example.melirutas.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class MeliRutasDatabaseMigrationTest {

    @Test
    fun migracion_4_5_creaResumenRutaSinSentenciasDestructivas() {
        val captura = CapturaSql()
        val db = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
            captura
        ) as SupportSQLiteDatabase

        val migracion = MeliRutasDatabase.MIGRACION_4_5

        assertEquals(4, migracion.startVersion)
        assertEquals(5, migracion.endVersion)

        migracion.migrate(db)

        assertEquals(1, captura.sentencias.size)
        val sql = captura.sentencias.single()
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS `resumen_ruta`"))
        assertTrue(sql.contains("`fecha` TEXT NOT NULL"))
        assertTrue(sql.contains("`paquetes` INTEGER"))
        assertTrue(sql.contains("`paradas` INTEGER"))
        assertTrue(sql.contains("`puntos` INTEGER"))
        assertTrue(sql.contains("`primerPedidoEn` INTEGER"))
        assertTrue(sql.contains("`ultimoPedidoEn` INTEGER"))
        assertTrue(sql.contains("PRIMARY KEY(`fecha`)"))
        assertFalse(sql.contains("DROP", ignoreCase = true))
        assertFalse(sql.contains("DELETE", ignoreCase = true))
        assertFalse(sql.contains("ALTER", ignoreCase = true))
        assertFalse(sql.contains("delivery_codes", ignoreCase = true))
        assertFalse(sql.contains("trips", ignoreCase = true))
    }

    private class CapturaSql : InvocationHandler {
        val sentencias = mutableListOf<String>()

        override fun invoke(proxy: Any, method: Method, args: Array<Any?>?): Any? {
            if (method.name == "execSQL" && args?.get(0) is String) {
                sentencias += args[0] as String
            }
            return null
        }
    }
}

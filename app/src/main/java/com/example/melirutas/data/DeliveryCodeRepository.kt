package com.example.melirutas.data

import com.example.melirutas.data.db.DeliveryCodeDao
import com.example.melirutas.data.db.DeliveryCodeEntity
import kotlinx.coroutines.flow.Flow

class DeliveryCodeRepository(private val dao: DeliveryCodeDao) {

    fun codigosDe(fecha: String): Flow<List<DeliveryCodeEntity>> = dao.codigosDe(fecha)

    fun fechasConCodigos(): Flow<List<String>> = dao.fechasConCodigos()

    suspend fun generarRango(fecha: String, desde: Int, hasta: Int): Int {
        if (desde > hasta) return 0

        val existentes = dao.codigosExistentes(fecha).toSet()
        val nuevos = (desde..hasta)
            .filterNot { it in existentes }
            .map { DeliveryCodeEntity(fecha = fecha, codigo = it) }

        if (nuevos.isEmpty()) return 0
        return dao.insertarTodos(nuevos).count { it != -1L }
    }

    suspend fun marcar(fecha: String, codigo: Int, entregadoEn: Long?) {
        dao.marcar(fecha, codigo, entregadoEn)
    }

    suspend fun borrarDia(fecha: String) = dao.borrarFecha(fecha)
}

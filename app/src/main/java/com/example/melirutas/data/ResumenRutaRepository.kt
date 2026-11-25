package com.example.melirutas.data

import com.example.melirutas.data.db.ResumenRutaDao
import com.example.melirutas.data.db.ResumenRutaEntity
import kotlinx.coroutines.flow.Flow

class ResumenRutaRepository(private val dao: ResumenRutaDao) {

    fun porFecha(fecha: String): Flow<ResumenRutaEntity?> = dao.porFecha(fecha)

    fun fechasConResumen(): Flow<List<String>> = dao.fechasConResumen()

    fun todosLosResumenes(): Flow<List<ResumenRutaEntity>> = dao.todosLosResumenes()

    suspend fun guardar(
        fecha: String,
        paquetes: Int?,
        paradas: Int?,
        puntos: Int?,
        primerPedidoEn: Long?,
        ultimoPedidoEn: Long?
    ) {
        dao.guardar(
            ResumenRutaEntity(
                fecha = fecha,
                paquetes = paquetes,
                paradas = paradas,
                puntos = puntos,
                primerPedidoEn = primerPedidoEn,
                ultimoPedidoEn = ultimoPedidoEn
            )
        )
    }
}

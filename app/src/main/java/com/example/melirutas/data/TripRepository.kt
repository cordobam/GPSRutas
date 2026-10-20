package com.example.melirutas.data

import com.example.melirutas.data.db.TripDao
import com.example.melirutas.data.db.TripEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Acceso a los viajes guardados en la base de datos. */
class TripRepository(private val dao: TripDao) {

    /** El viaje abierto (a medio cargar) o null si no hay ninguno. */
    fun viajeAbierto(): Flow<TripEntity?> = dao.viajeAbierto().map { it.firstOrNull() }

    /** Viajes finalizados, del más reciente al más viejo. */
    val historial: Flow<List<TripEntity>> = dao.historial()

    /**
     * Crea o actualiza el viaje abierto con lo que el usuario cargó. No hace nada
     * si todavía no hay km (así no se crean viajes fantasma por solo cambiar el
     * rendimiento o el precio).
     */
    suspend fun guardarBorrador(
        kmInicial: Double?,
        kmFinal: Double?,
        rendimiento: Double?,
        precioPorLitro: Double?
    ) {
        if (kmInicial == null && kmFinal == null) return

        val abierto = viajeAbierto().first()
        if (abierto == null) {
            dao.insertar(
                TripEntity(
                    fechaInicio = System.currentTimeMillis(),
                    fechaFin = null,
                    kmInicial = kmInicial,
                    kmFinal = kmFinal,
                    rendimiento = rendimiento,
                    precioPorLitro = precioPorLitro
                )
            )
        } else {
            dao.actualizar(
                abierto.copy(
                    kmInicial = kmInicial,
                    kmFinal = kmFinal,
                    rendimiento = rendimiento,
                    precioPorLitro = precioPorLitro
                )
            )
        }
    }

    /**
     * Termina el viaje en curso: si tiene km cargados lo pasa al historial; si está
     * vacío lo descarta. Queda listo para arrancar un viaje nuevo.
     */
    suspend fun nuevoViaje() {
        val abierto = viajeAbierto().first() ?: return
        if (abierto.kmInicial == null && abierto.kmFinal == null) {
            dao.eliminar(abierto)
        } else {
            dao.actualizar(abierto.copy(fechaFin = System.currentTimeMillis()))
        }
    }
}

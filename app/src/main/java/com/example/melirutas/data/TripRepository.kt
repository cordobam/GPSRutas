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

    /**
     * Viajes finalizados de los últimos 7 días, del más reciente al más viejo.
     * El umbral se calcula en cada emisión, así que se renueva solo.
     */
    fun historial(): Flow<List<TripEntity>> = dao.historial().map { viajes ->
        val desde = System.currentTimeMillis() - ULTIMA_SEMANA_MILLIS
        viajes.filter { (it.fechaFin ?: 0L) >= desde }
    }

    /** Borra un viaje del historial. */
    suspend fun eliminarViaje(trip: TripEntity) = dao.eliminar(trip)

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

/** Cuánto tiempo hacia atrás muestra el historial de la pestaña Costos. */
private const val ULTIMA_SEMANA_MILLIS = 7L * 24 * 60 * 60 * 1000

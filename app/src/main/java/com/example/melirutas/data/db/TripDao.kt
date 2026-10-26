package com.example.melirutas.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    /** El viaje abierto (sin cerrar). Devuelve 0 o 1 fila; el repositorio toma la primera. */
    @Query("SELECT * FROM trips WHERE fechaFin IS NULL ORDER BY id DESC LIMIT 1")
    fun viajeAbierto(): Flow<List<TripEntity>>

    /** Viajes finalizados, del más reciente al más viejo según la fecha del viaje. */
    @Query("SELECT * FROM trips WHERE fechaFin IS NOT NULL ORDER BY fechaInicio DESC")
    fun historial(): Flow<List<TripEntity>>

    @Insert
    suspend fun insertar(trip: TripEntity): Long

    @Update
    suspend fun actualizar(trip: TripEntity)

    /** Marca (o desmarca, con null) el pago de la nafta de varios viajes a la vez. */
    @Query("UPDATE trips SET pagadoEn = :cuando WHERE id IN (:ids)")
    suspend fun actualizarPago(ids: List<Long>, cuando: Long?)

    @Delete
    suspend fun eliminar(trip: TripEntity)
}

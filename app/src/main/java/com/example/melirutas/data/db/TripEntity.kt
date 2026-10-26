package com.example.melirutas.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Viaje en curso o finalizado.
 *
 * Los campos numéricos son nullable para poder guardar un viaje a medio cargar
 * (mientras el usuario tipea). El viaje "abierto" es el que tiene [fechaFin] en null;
 * al cerrarlo pasa al historial. [pagadoEn] en null significa que la nafta todavía
 * no se rindió; con fecha, que ya se pagó.
 */
@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fechaInicio: Long,
    val fechaFin: Long?,
    val kmInicial: Double?,
    val kmFinal: Double?,
    val rendimiento: Double?,
    val precioPorLitro: Double?,
    val pagadoEn: Long? = null
)

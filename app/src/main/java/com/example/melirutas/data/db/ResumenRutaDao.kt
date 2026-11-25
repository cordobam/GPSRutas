package com.example.melirutas.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ResumenRutaDao {

    @Query("SELECT * FROM resumen_ruta WHERE fecha = :fecha")
    fun porFecha(fecha: String): Flow<ResumenRutaEntity?>

    @Query("SELECT DISTINCT fecha FROM resumen_ruta ORDER BY fecha DESC")
    fun fechasConResumen(): Flow<List<String>>

    @Query("SELECT * FROM resumen_ruta ORDER BY fecha DESC")
    fun todosLosResumenes(): Flow<List<ResumenRutaEntity>>

    @Upsert
    suspend fun guardar(resumen: ResumenRutaEntity)
}

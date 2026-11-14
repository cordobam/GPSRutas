package com.example.melirutas.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryCodeDao {

    @Query("SELECT * FROM delivery_codes WHERE fecha = :fecha ORDER BY codigo ASC")
    fun codigosDe(fecha: String): Flow<List<DeliveryCodeEntity>>

    @Query("SELECT codigo FROM delivery_codes WHERE fecha = :fecha")
    suspend fun codigosExistentes(fecha: String): List<Int>

    @Query("SELECT DISTINCT fecha FROM delivery_codes ORDER BY fecha DESC")
    fun fechasConCodigos(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(codigos: List<DeliveryCodeEntity>): List<Long>

    @Query("UPDATE delivery_codes SET entregadoEn = :cuando WHERE fecha = :fecha AND codigo = :codigo")
    suspend fun marcar(fecha: String, codigo: Int, cuando: Long?)

    @Query("DELETE FROM delivery_codes WHERE fecha = :fecha")
    suspend fun borrarFecha(fecha: String)
}

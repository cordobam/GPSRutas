package com.example.melirutas.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resumen_ruta")
data class ResumenRutaEntity(
    @PrimaryKey val fecha: String,
    val paquetes: Int? = null,
    val paradas: Int? = null,
    val puntos: Int? = null,
    val primerPedidoEn: Long? = null,
    val ultimoPedidoEn: Long? = null
)

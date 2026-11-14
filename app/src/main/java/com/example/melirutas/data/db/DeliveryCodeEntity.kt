package com.example.melirutas.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "delivery_codes",
    indices = [Index(value = ["fecha", "codigo"], unique = true)]
)
data class DeliveryCodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: String,
    val codigo: Int,
    val entregadoEn: Long? = null
)

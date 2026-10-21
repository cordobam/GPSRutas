package com.example.melirutas.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TripEntity::class], version = 1, exportSchema = false)
abstract class MeliRutasDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var instance: MeliRutasDatabase? = null

        fun getInstance(context: Context): MeliRutasDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MeliRutasDatabase::class.java,
                    "meli_rutas.db"
                ).build().also { instance = it }
            }
    }
}

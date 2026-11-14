package com.example.melirutas.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TripEntity::class, DeliveryCodeEntity::class], version = 4, exportSchema = false)
abstract class MeliRutasDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao

    abstract fun deliveryCodeDao(): DeliveryCodeDao

    companion object {
        @Volatile
        private var instance: MeliRutasDatabase? = null

        /** Agrega el estado de pago sin perder los viajes ya guardados. */
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE trips ADD COLUMN pagadoEn INTEGER")
            }
        }

        /** Agrega el pago del viaje y el peaje sin perder los viajes ya guardados. */
        private val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE trips ADD COLUMN pagoViaje REAL")
                db.execSQL("ALTER TABLE trips ADD COLUMN peajeMonto REAL")
                db.execSQL("ALTER TABLE trips ADD COLUMN peajeDireccion TEXT")
            }
        }

        /** Agrega los códigos de entrega por día sin perder los viajes ya guardados. */
        private val MIGRACION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `delivery_codes` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`fecha` TEXT NOT NULL, " +
                        "`codigo` INTEGER NOT NULL, " +
                        "`entregadoEn` INTEGER)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_delivery_codes_fecha_codigo` " +
                        "ON `delivery_codes` (`fecha`, `codigo`)"
                )
            }
        }

        fun getInstance(context: Context): MeliRutasDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MeliRutasDatabase::class.java,
                    "meli_rutas.db"
                )
                    .addMigrations(MIGRACION_1_2, MIGRACION_2_3, MIGRACION_3_4)
                    .build()
                    .also { instance = it }
            }
    }
}

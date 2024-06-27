package com.example.mad3d.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Poi::class], version = 2)
abstract class PoiDatabase : RoomDatabase() {
    abstract fun getPoiDao(): PoiDao

    companion object {
        @Volatile
        private var DATABASE_INSTANCE: PoiDatabase? = null
        fun getDatabase(context: Context): PoiDatabase {

            //synchronized makes sure it will only run once on the thread

            return DATABASE_INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    PoiDatabase::class.java,
                    "poi_database"
                )
                    .build()
                DATABASE_INSTANCE = instance
                instance
            }
        }
    }
}
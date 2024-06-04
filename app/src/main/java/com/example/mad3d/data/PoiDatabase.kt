package com.example.mad3d.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Poi::class], version = 1)
abstract class PoiDatabase : RoomDatabase() {
    abstract fun getPoiDao(): PoiDao

    companion object {
        fun createDatabase(context: Context): PoiDatabase {
            return Room.databaseBuilder(
                context,
                PoiDatabase::class.java,
                "poi_database"
            )
                .build()
        }
    }
}
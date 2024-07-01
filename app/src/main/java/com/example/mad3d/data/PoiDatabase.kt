package com.example.mad3d.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Poi::class], version = 2)
abstract class PoiDatabase : RoomDatabase() {
    // abstract function to get the DAO for POIs
    abstract fun getPoiDao(): PoiDao

    companion object {
        // Volatile ensures the instance is visible to all threads
        @Volatile
        private var DATABASE_INSTANCE: PoiDatabase? = null

        // function to get the singleton database instance
        fun getDatabase(context: Context): PoiDatabase {
            // synchronized block to ensure only one instance is created
            return DATABASE_INSTANCE ?: synchronized(this) {
                // create the database instance if it doesn't exist
                val instance = Room.databaseBuilder(
                    context,
                    PoiDatabase::class.java,
                    "poi_database"
                ).build()
                // assign the created instance to the DATABASE_INSTANCE variable
                DATABASE_INSTANCE = instance
                // return the created instance
                instance
            }
        }
    }
}

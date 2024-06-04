package com.example.mad3d.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Poi::class], version = 1)
abstract class PoiDatabase : RoomDatabase() {
    abstract fun getPoiDao(): PoiDao
}
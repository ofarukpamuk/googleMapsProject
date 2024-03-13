package com.example.googlemapsproject.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.googlemapsproject.model.Place

// veri tabanımızı olusturuyoruz
@Database(entities = arrayOf(Place::class), version = 1)
abstract class PlaceDatabase:RoomDatabase() {
    abstract fun placeDao():PlaceDao
}
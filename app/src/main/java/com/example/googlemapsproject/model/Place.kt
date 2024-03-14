package com.example.googlemapsproject.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity // Entity(place) şeklinde de yazabiliriz table name'i alması için ama parantez boş olunca da zaten table ismini Place alacak
data class Place(

    @ColumnInfo(name = "name") // kolon isimlerini belirtdik  değişken adı ile kolon ismi aynı olmasa da olur ama mantıklı olanı böyle olması
    var name : String ,

    @ColumnInfo(name = "latitude")
    var latitude:Double,

    @ColumnInfo(name = "longitude")
    var longitude : Double
):Serializable{

@PrimaryKey(autoGenerate = true) // otomatik id olusturma
var id = 0
}

package com.example.googlemapsproject.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.googlemapsproject.model.Place
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface PlaceDao { // insert delete update vs kodlarımızı yazıyoruz veritabanı için
    @Query("SELECT * FROM Place") //"SELECT * FROM Place Where id= :İd" fonksiyonda alınan değeri : yaparak alıp veri tabanında işleyebiliriz
    fun getALl():Flowable<List<Place>> // flowable rxjava da asenkron yapılacak işlemlerde geriye bir veri dönecek ise kullanılır
    @Insert
    fun insert(place: Place): Completable // completable ise rxjava da geriye döndürülecek bir işlem yok ama async oldugu için
    @Delete
    fun delete(place: Place): Completable


}
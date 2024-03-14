package com.example.googlemapsproject.view

import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.googlemapsproject.R
import com.google.android.material.snackbar.Snackbar

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.googlemapsproject.databinding.ActivityMapsBinding
import com.example.googlemapsproject.model.Place
import com.example.googlemapsproject.roomdb.PlaceDao
import com.example.googlemapsproject.roomdb.PlaceDatabase
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.annotations.Async.Schedule


class MapsActivity : AppCompatActivity(), OnMapReadyCallback , GoogleMap.OnMapLongClickListener{ // long Click listener uzun tıklamalarda haritada işaret koymamız için

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager : LocationManager
    private lateinit var locationListener : LocationListener
    private lateinit var permissionLauncher : ActivityResultLauncher<String> // string alma sebebi izinler manifeste string olarak gözüküyor
    private lateinit var sharedPreferences : SharedPreferences
    private var trackBoolean : Boolean? = null
    private var selectedLatitude : Double? = null
    private var selectedLongitude : Double? = null
    private lateinit var db :PlaceDatabase
    private lateinit var placeDao : PlaceDao
    val compositDisposable = CompositeDisposable()// tek kullanımlık kullan at bellekte yer tutmaması içi rxJava kullanır
   var placeFromMain : Place? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Snackbar.make(binding.root, "Bu basit bir Snackbar", Snackbar.LENGTH_LONG).show()
        sharedPreferences = this.getSharedPreferences("com.example.googlemapsproject", MODE_PRIVATE)
       trackBoolean = false
        selectedLatitude = 0.0
        selectedLongitude = 0.0
        db = Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places").build()
        placeDao = db.placeDao()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        registerLauncher()
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)// uzun tıklamada çalışacak listenerin dahil edildigi anlamina gelir yukarda miras aldık sınıfta zaten
        val intent=  Intent()
        val info = intent.getStringExtra("info")
        if (info == "new"){
            binding.saveButton.visibility =View.GONE
            binding.deleteButton.visibility =View.GONE
            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
            locationListener= object : LocationListener{
                override fun onLocationChanged(location: Location) {
                    trackBoolean = sharedPreferences.getBoolean("trackBoolean", false )
                    if (trackBoolean == false ){
                        val userLocation = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply()
                    }



                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                    super.onStatusChanged(provider, status, extras)

                }

            }

            if (ContextCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar.make(binding.root, "permission needed for location ", Snackbar.LENGTH_LONG).setAction("give permission"){// set action yani yıklanırsa ne olacagı
                        // request permission
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()
                }else{
                    // request permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                // izin zaten verilmiş
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) // kullanıcıdan en son konumunu getirmek için kullanıyoruz
                if (lastLocation != null){
                    val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                }
                mMap.isMyLocationEnabled= true // konumu etkinleştirdik mi yani nerede oldugu kullanıcının Navigasyon yön işaretini aktif etme

            }
        }else{
            mMap.clear()
            placeFromMain =intent.getSerializableExtra("selectedPlace")as? Place
            placeFromMain?.let {
                val latLang = LatLng(it.latitude, it.longitude)
                mMap.addMarker(MarkerOptions().position(latLang ).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang,15f))
                binding.placeText.setText(it.name)
                binding.saveButton.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE
                binding.saveButton.isEnabled= false// tıklanmaz yap butonu

            }

        }

    }
    private fun registerLauncher() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result->

            if (result ){
                if (ContextCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation != null){
                        val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                    }
                }

            }else{
                Toast.makeText( this@MapsActivity,"permission needed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapLongClick(p0: LatLng) {
       //  mMap.clear() kullanıcının her marker birakmasinda diger markerlari ekrandan kaldirir
        mMap.addMarker(MarkerOptions().position(p0))
        selectedLatitude = p0.latitude
        selectedLongitude = p0.longitude
    // hangi pozisyonda oldugumuzu callback function verdigi icin isaretci koymak kolay hake geldi
    }

    fun save(view : View){
    val name =binding.placeText.text.toString()
    val lat=    selectedLatitude
        val long = selectedLongitude
    val place  = lat?.let { long?.let { it1 -> Place(name, it, it1) } }
        place?.let {
            compositDisposable.add(placeDao.insert(it) // ekle
                .subscribeOn(Schedulers.io()) // işlemi io da yap
                .observeOn(AndroidSchedulers.mainThread()) // sonucu mainthread de ele alacagiz
                .subscribe(this::handlerResponse) // subscribe() da bu işlem bittikten sonra ne olacagını bu fonksiyona vermemiz gerekir
            )
        }

    }
    private fun handlerResponse(){
        val intent = Intent(this, MainActivity :: class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)// kalan sayfaları bellekten sil
        startActivity(intent)

    }
    fun delete(view : View){
        placeFromMain?.let {
            compositDisposable.add(placeDao.delete(it).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this:: handlerResponse  ))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositDisposable.clear()
    }
}

//  val eiffel = LatLng(48.853915, 2.2913515)  manuel location notifier
// mMap.addMarker(MarkerOptions().position(eiffel).title("eiffel tower"))
// mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eiffel,15f))

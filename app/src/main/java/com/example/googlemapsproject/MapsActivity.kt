package com.example.googlemapsproject

import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.Instrumentation.ActivityResult
import android.content.SharedPreferences
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Snackbar
import com.google.android.material.snackbar.Snackbar

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.googlemapsproject.databinding.ActivityMapsBinding


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Snackbar.make(binding.root, "Bu basit bir Snackbar", Snackbar.LENGTH_LONG).show()
        sharedPreferences = this.getSharedPreferences("com.example.googlemapsproject", MODE_PRIVATE)
       trackBoolean = false
        selectedLatitude = 0.0
        selectedLongitude = 0.0
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        registerLauncher()
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)// uzun tıklamada çalışacak listenerin dahil edildigi anlamina gelir yukarda miras aldık sınıfta zaten

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
}

//  val eiffel = LatLng(48.853915, 2.2913515)  manuel location notifier
// mMap.addMarker(MarkerOptions().position(eiffel).title("eiffel tower"))
// mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eiffel,15f))

package com.eskisehir.letsgo.Add

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.*
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.EventbusDataEvents
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_location.*
import org.greenrobot.eventbus.EventBus
import java.util.*


//haritadan lokasyon çekme ve marker ekleme kısımları burada yapılacak

class LocationActivity : AppCompatActivity() {
    lateinit var mapFragment : SupportMapFragment
    lateinit var googleMap: GoogleMap
    //lateinit ile GoogleMap tipinde googleMap objesini sonradan tanımlayacağımızı söylemiş olduk

    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    var hasLocation = false
    var isMarked = false
    var lg = 0.0
    var lt = 0.0
    var cityName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
//supportFragmentManager bir yardımcı sınıf gibi düşün.fragmentlarla çalışmamız için yardımcı bir metot çağırıyor.
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it
            googleMap.isMyLocationEnabled = true

            //mapin üstüne tıklanması halinde burada tıklama işlemi dinleniyor
            googleMap.setOnMapClickListener { it ->
                //allPoints.add(it)
                googleMap.clear()
                //dokunulan yere marker koyuldu
                googleMap.addMarker(MarkerOptions().position(it))
                println(""+it.latitude+" "+it.longitude)

                val geocoder = Geocoder(this, Locale.getDefault())
                val list:List<Address> = geocoder.getFromLocation(it.latitude,it.longitude,1)

                isMarked = true
                lt = it.latitude
                lg = it.longitude
                //markerin olduğu yerde şehir yoksa uyarı verecek
                if (list[0].adminArea == null){
                    Toast.makeText(this,"Lütfen şehir bölgesinden bir yer seçiniz",Toast.LENGTH_LONG).show()
                    konumOnayla.setBackgroundResource(R.drawable.button_inactive)
                    konumOnayla.isEnabled = false
                    konumOnayla.setTextColor(ContextCompat.getColor(this,R.color.mavi))
                }else{

                    //her şey yolunca giderse onay butonu aktifleşecek
                    cityName = list[0].adminArea

                    konumOnayla.setBackgroundResource(R.drawable.button_active)
                    konumOnayla.isEnabled = true
                    konumOnayla.setTextColor(ContextCompat.getColor(this,R.color.beyaz))
                }



            }

        })

        //kendi lokasyonumuz için butona tıklanınca burası çalışacak
        applyPin.setOnClickListener {
            hasLocation = false
            getLocation()
        }

        konumOnayla.setOnClickListener {

            EventBus.getDefault().postSticky(EventbusDataEvents.lokasyonBilgileriniGonder(cityName,lg,lt))
            onBackPressed()

        }


    }

    //tıklayarak veye kendi lokasyonumuz ile marker koymak
    fun setMark(lon:Double,lat:Double){
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap.clear()
            googleMap = it
            googleMap.isMyLocationEnabled = true
            val location1 = LatLng(lat, lon)
            googleMap.addMarker(MarkerOptions().position(location1).title("My Location"))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location1, 3f))


            val geocoder = Geocoder(this, Locale.getDefault())
            val list:List<Address> = geocoder.getFromLocation(lat,lon,1)


            isMarked = true
            lt = lat
            lg = lon
            cityName = list.get(0).adminArea
            konumOnayla.setBackgroundResource(R.drawable.button_active)
            konumOnayla.isEnabled = true
            konumOnayla.setTextColor(ContextCompat.getColor(this,R.color.beyaz))


        })

    }



//gps veya network ile konum alma yeri, Hangisi daha hızlı verirse o konum ile dönüş yapıyor
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork) {

            if (hasGps) {
                Log.d("CodeAndroidLocation", "hasGps")
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if (location != null && !hasLocation) {

                            locationGps = location
                            Log.d("CodeAndroidLocation", " GPS Latitude : " + locationGps!!.latitude)
                            Log.d("CodeAndroidLocation", " GPS Longitude : " + locationGps!!.longitude)
                            println(""+locationGps!!.longitude+ " "+ locationGps!!.latitude)
                            setMark(locationGps!!.longitude,locationGps!!.latitude)
                            hasLocation = true
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                    }

                    override fun onProviderEnabled(provider: String?) {

                    }

                    override fun onProviderDisabled(provider: String?) {

                    }

                })

                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null)
                    locationGps = localGpsLocation
            }
            if (hasNetwork) {
                Log.d("CodeAndroidLocation", "hasGps")
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0F, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if (location != null && !hasLocation) {
                            locationNetwork = location
                            Log.d("CodeAndroidLocation", " Network Latitude : " + locationNetwork!!.latitude)
                            Log.d("CodeAndroidLocation", " Network Longitude : " + locationNetwork!!.longitude)
                            setMark(locationNetwork!!.longitude,locationNetwork!!.latitude)
                            hasLocation=true
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                    }

                    override fun onProviderEnabled(provider: String?) {

                    }

                    override fun onProviderDisabled(provider: String?) {

                    }

                })

                val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null)
                    locationNetwork = localNetworkLocation
            }

        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }


}

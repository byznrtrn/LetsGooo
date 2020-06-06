package com.eskisehir.letsgo.Search


import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.eskisehir.letsgo.Model.Ilan
import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.EventbusDataEvents
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ui.IconGenerator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

//arama sonuçlarının harita üzerinde lg ve lt değerlerine göre işaretlenip gösterildiği yer

class SearchResultMapActivity : AppCompatActivity() {

    lateinit var mapFragment : SupportMapFragment
    lateinit var googleMap: GoogleMap
    lateinit var gelenResults : ArrayList<Ilan>



    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    var hasLocation = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result_map)


    }


    private fun setupResult() {


        var markers : ArrayList<MarkerOptions> = ArrayList()

        mapFragment = supportFragmentManager.findFragmentById(R.id.resultMap) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it
            googleMap.isMyLocationEnabled = true

            var index = 0
            //ilan konumlarına marker eklenir
            //marker olarak custom marker kullanıldı
            for (ilan in gelenResults){
                val location= LatLng(ilan.lt!!,ilan.lg!!)
                var iconGen = IconGenerator(this)
                iconGen.setTextAppearance(R.style.textIcerik);
                iconGen.setBackground(ContextCompat.getDrawable(this,R.drawable.marker_background))
                val bitmap =iconGen.makeIcon(""+ilan.price+"tl")
                val marker = MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(location).title(""+index)



               // var markerOptions = MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("Text"))).position(LatLng(lat, , from, , database, lon, , from, , database)).anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV())


                googleMap.addMarker(marker)
                markers.add(marker)
            }

            var builder = LatLngBounds.Builder();
            for (marker in markers){
                builder.include(marker.position)
            }

            var bounds = builder.build()
            var padding = 10
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,padding)
            googleMap.animateCamera(cameraUpdate);



            //markerlara tıklanınca detay sayfasına yönlendirme yapılıyor
            googleMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener{
                override fun onMarkerClick(p0: Marker?): Boolean {

                    val position = p0!!.title.toInt()

                    val intent = Intent(this@SearchResultMapActivity,IlanDetayActivity::class.java)
                    EventBus.getDefault().postSticky(EventbusDataEvents.ilanBilgileriniGonder(gelenResults[position]))
                    startActivity(intent)



                    return false
                }





            })






        })


    }



    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    internal fun onResultBilgileri(results: EventbusDataEvents.aramaSonucBilgileriniGonder){

        gelenResults = results.ilanlar


        setupResult()



    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

}

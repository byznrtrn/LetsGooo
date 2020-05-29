package com.example.letsgoo.Add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.getSystemService
import com.eskisehir.letsgo.Model.Car
import com.eskisehir.letsgo.Model.Ilan
import com.eskisehir.letsgo.Profil.AddCar

import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.EventbusDataEvents
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.datatype.DatatypeConstants.MONTHS
import kotlin.collections.HashMap

/**
 * İlan verme sayfası
 */
class AddFragment : Fragment() {

    //haritatan seçilecek olan şehir ve şehrin lokasyon bilgilerini class için her yerden ulaşmak için burda tanımladım
    lateinit var rootView:View
    var city = ""
    var lg = 0.0
    var lt = 0.0
    lateinit var mRef : DatabaseReference
    lateinit var car : Car

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_add, container, false)

        mRef = FirebaseDatabase.getInstance().reference

        //ilan vermek için araba var mı yok diye kontrol ediliyor
        mRef.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("car")
                .addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        println(p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            car = p0.getValue(Car::class.java)!!

                            if (car.model.equals("")){
                                //araba yoksa
                                noCarLayout.visibility = View.VISIBLE
                                scrollView2.visibility = View.GONE
                            }else{
                                //araba varsa
                                noCarLayout.visibility = View.GONE
                                scrollView2.visibility = View.VISIBLE
                            }
                            init()
                        }
                    }

                })








        return rootView
    }

    private fun init() {

        //araba yoksa araba ekleme sayfasına yönlendiren buton click özelliği eklendi
        imgAddCar.setOnClickListener {
            val intent = Intent(activity!!, AddCar::class.java)
            EventBus.getDefault().postSticky(EventbusDataEvents.arabaBilgileriniGonder(car,"new"))
            startActivity(intent)
        }

        //tek tek ekleme sayfasındaki bölümlerin bilgi alma metodları eklendi

        //nerden yola çıkılacaksa haritan seçim yapıldı
        rootView.addNerden.setOnClickListener {
            permissionCheck()
        }
        //date ve time için picker kullanıldı
        rootView.addDate.setOnClickListener {

            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)


            val dpd = DatePickerDialog(activity, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                // Display Selected date in textbox
                val m = monthOfYear +1
                addDate.setText("$dayOfMonth / $m / $year")
            }, year, month, day)

            dpd.show()


        }

        rootView.addTime.setOnClickListener {

            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                rootView.addTime.setText(SimpleDateFormat("HH:mm").format(cal.time))
            }
            TimePickerDialog(activity, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
//nereye gidilecekse 81 ili barındıran spinner açıldı
        rootView.nereyeLayout.setOnClickListener {

            val popup : PopupMenu = PopupMenu(context,rootView.addNereye)
            popup.inflate(R.menu.city_menu)
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
                override fun onMenuItemClick(item: MenuItem?): Boolean {

                    rootView.addNereye.setText(item!!.title)

                    return false
                }

            })

            popup.show()

        }

        rootView.addNereye.setOnClickListener {
            rootView.nereyeLayout.performClick()
        }


        // ilan ver buyonuna basılınca boş alan var mı kontolleri yapılmalı
        rootView.ilanVerButton.setOnClickListener {

            var bos_alan = 0

            if (rootView.addNerden.text.toString().equals("")){
                bos_alan++
            }

            if (rootView.addNereye.text.toString().equals("")){
                bos_alan++
            }

            if (rootView.addDate.text.toString().equals("")){
                bos_alan++
            }

            if (rootView.addTime.text.toString().equals("")){
                bos_alan++
            }

            if (rootView.addPrice.text.toString().equals("")){
                bos_alan++
            }

            if (rootView.addCapasity.text.toString().equals("")){
                bos_alan++
            }

            if (!(bos_alan == 0)){
                Toast.makeText(activity,"Lütfen tüm alanları doldurduğunuza emin olun!",Toast.LENGTH_LONG).show()
            }else{
                //eğer boş alan yoksa ilan oluşturuluyor
                val userId = FirebaseAuth.getInstance().currentUser!!.uid+"&true"
                val rota = rootView.addNerden.text.toString()+"&"+rootView.addNereye.text.toString()+"&"+rootView.addDate.text.toString()
                val time = rootView.addTime.text.toString()
                val price = rootView.addPrice.text.toString().toInt()
                val capasity =rootView.addCapasity.text.toString().toInt()
                val key = mRef.push().key!!
                val sdf = SimpleDateFormat("d / MM / yyyy HH:mm")
                val date = sdf.parse(rootView.addDate.text.toString()+" "+time)
                val unix =date.time / 1000L;
                val ilan = Ilan(userId,key,rota,time,capasity,price,lt,lg,unix)


                mRef.child("ilanlar").child(key).setValue(ilan)
                        .addOnCompleteListener { p0 ->
                            if (p0.isSuccessful){
                                println("başarılı")
                                Toast.makeText(activity,"İlan Oluşturuldu",Toast.LENGTH_SHORT).show()
                                rootView.addNerden.setText("")
                                rootView.addNereye.setText("")
                                rootView.addDate.setText("")
                                rootView.addTime.setText("")
                                rootView.addCapasity.setText("")
                                rootView.addPrice.setText("")

                                val imm : InputMethodManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(activity!!.currentFocus.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)


                                var menu = activity!!.bottomNavigationView.menu
                                menu.performIdentifierAction(R.id.ic_home,0)

                            }else{
                                println("başarısız++++++++++++++")
                                Toast.makeText(activity,"Bir hatadan dolayı ilan verilemedi. Lütfen tekrar deneyiniz",Toast.LENGTH_LONG).show()
                            }
                        }

            }



        }


    }



    //harita için izin alırken dexter kütüphanesi kullanıldı
    private fun permissionCheck() {
        Dexter.withActivity(activity)
                .withPermissions(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        if (p0!!.areAllPermissionsGranted()){
                            val intent = Intent(activity!!,LocationActivity::class.java)
                            startActivity(intent)
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                            p0: MutableList<PermissionRequest>?,
                            p1: PermissionToken?
                    ) {
                        println("Bir izin verilmedi")
                    }

                }).check()

    }


    //event bus ile harita tarafından gönderilen lokasyon bilgileri yakalandı
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    internal fun onMyLocation(lokasyonBilgileri: EventbusDataEvents.lokasyonBilgileriniGonder){

        city = lokasyonBilgileri.City!!
        lt = lokasyonBilgileri.lt!!
        lg = lokasyonBilgileri.lg!!

        addNerden.setText(city)


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

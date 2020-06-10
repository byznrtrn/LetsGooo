package com.eskisehir.letsgo.Add


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.eskisehir.letsgo.Model.Car
import com.eskisehir.letsgo.Model.Ilan
import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.EventbusDataEvents
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_ilan_duzenle.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

class IlanDuzenleActivity : AppCompatActivity() {
    lateinit var gelenIlan : Ilan
//ilan sayfasındaki objeleri kullanabilmek için inherit ettik
    var city = ""
    var lg = 0.0
    var lt = 0.0
    lateinit var mRef : DatabaseReference
    lateinit var car : Car

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ilan_duzenle)

        mRef = FirebaseDatabase.getInstance().reference

    }



    //gelen ilan verilerini uygun şekilde bölümlere dolduruyoruz
    private fun setupIlanBilgileri() {

        addNerden.setText(gelenIlan.rotaDate!!.split("&")[0])
        addNereye.setText(gelenIlan.rotaDate!!.split("&")[1])
        addDate.setText(gelenIlan.rotaDate!!.split("&")[2])
        addTime.setText(gelenIlan.saat)
        addPrice.setText(""+gelenIlan.price)
        addCapasity.setText(""+gelenIlan.kapasite!!)
        lg = gelenIlan.lg!!
        lt = gelenIlan.lt!!


        ilanVerButton.setOnClickListener {

            var bos_alan = 0

            if (addNerden.text.toString().equals("")){
                bos_alan++
            }

            if (addNereye.text.toString().equals("")){
                bos_alan++
            }

            if (addDate.text.toString().equals("")){
                bos_alan++
            }

            if (addTime.text.toString().equals("")){
                bos_alan++
            }

            if (addPrice.text.toString().equals("")){
                bos_alan++
            }

            if (addCapasity.text.toString().equals("")){
                bos_alan++
            }

            if (!(bos_alan == 0)){
                Toast.makeText(this,"Lütfen tüm alanları doldurduğunuza emin olun!", Toast.LENGTH_LONG).show()
            }else{



                //güncelleme yapılacağı için tüm kısımlar tekrar elde edilip aynı id ile veritabanına eklenecek
                val rota = addNerden.text.toString()+"&"+addNereye.text.toString()+"&"+addDate.text.toString()
                val time = addTime.text.toString()
                val price = addPrice.text.toString().toInt()
                val capasity =addCapasity.text.toString().toInt()
                val sdf = SimpleDateFormat("d / MM / yyyy HH:mm")
                val date = sdf.parse(addDate.text.toString()+" "+time)
                val unix =date.time / 1000L;

                var ilan=HashMap<String,Any>()
                ilan.put("rotaDate",rota)
                ilan.put("saat",time)
                ilan.put("price",price)
                ilan.put("kapasite",capasity)
                ilan.put("lg",lg)
                ilan.put("lt",lt)
                ilan.put("unix",unix)


                gelenIlan.rotaDate = rota
                gelenIlan.saat = time
                gelenIlan.price = price
                gelenIlan.kapasite = capasity
                gelenIlan.lg = lg
                gelenIlan.lt = lt


            //updateChilden metodunnu sadece burada kullandık.When calling updateChildren(), you can update lower-level child values by specifying a path for the key.
                mRef.child("ilanlar").child(gelenIlan.ilanId!!).updateChildren(ilan).addOnCompleteListener {
                    p0->
                    if (p0.isSuccessful){
                        Toast.makeText(this,"İlanınız Güncellenmiştir", Toast.LENGTH_LONG).show()
                        EventBus.getDefault().postSticky(EventbusDataEvents.ilanBilgileriniGonder(gelenIlan))
                        onBackPressed()

                    }
                }





            }




        }



        addNerden.setOnClickListener {
            permissionCheck()
        }

       addDate.setOnClickListener {

            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)


            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                // Display Selected date in textbox
                val m = monthOfYear +1
                addDate.setText("$dayOfMonth / $m / $year")
            }, year, month, day)

            dpd.show()


        }

        addTime.setOnClickListener {

            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                addTime.setText(SimpleDateFormat("HH:mm").format(cal.time))
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        nereyeLayout.setOnClickListener {

            val popup : PopupMenu = PopupMenu(this,addNereye)
            popup.inflate(R.menu.city_menu)
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
                override fun onMenuItemClick(item: MenuItem?): Boolean {

                    addNereye.setText(item!!.title)

                    return false
                }

            })

            popup.show()

        }

        addNereye.setOnClickListener {
           nereyeLayout.performClick()
        }



        //ilanı silmek için kişiden onay isteyen builder gösterilecek
        //ilan silindiğinde ona dair her şey silinmiş olacak
        silButon.setOnClickListener {


            val builder = AlertDialog.Builder(this)
            builder.setTitle("İlanı Sil")
            builder.setMessage("İlanı Silmek istediğinize emin misiniz?")
            builder.setNegativeButton("Onayla"){dialog,which ->

                mRef.child("ilanlar").child(gelenIlan.ilanId!!).child("basvuranlar").addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        println(p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            for (users in p0.children){
                                mRef.child("basvurular").child(users.key!!).child(gelenIlan.ilanId!!).removeValue() //belirtilen referanstaki datayı silmek için removeValue()
                            }
                            mRef.child("ilanlar").child(gelenIlan.ilanId!!).removeValue().addOnCompleteListener {
                                Toast.makeText(this@IlanDuzenleActivity,"İlan Silindi",Toast.LENGTH_LONG).show()
                                EventBus.getDefault().postSticky(EventbusDataEvents.modeGonder("Sil"))
                                onBackPressed()
                            }
                        }else{
                            mRef.child("ilanlar").child(gelenIlan.ilanId!!).removeValue().addOnCompleteListener {
                                Toast.makeText(this@IlanDuzenleActivity,"İlan Silindi",Toast.LENGTH_LONG).show()
                                EventBus.getDefault().postSticky(EventbusDataEvents.modeGonder("Sil"))

                                onBackPressed()
                            }
                        }
                    }

                })



            }
            val dialog: AlertDialog = builder.create()
            dialog.show()

        }



    }

    private fun permissionCheck() {
        Dexter.withActivity(this)
                .withPermissions(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        if (p0!!.areAllPermissionsGranted()){
                            val intent = Intent(this@IlanDuzenleActivity,LocationActivity::class.java)
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

    /*
    Kanala sticky bir event gönderdiğinizde, bu event önbellekte saklanır.
    Yeni bir Activity veya Fragment o event kanalına abone olduğunda,
    yeni bir event’in yayınlanmasını beklemeden en son yayınlanan ve önbellekte saklanan yapışkan event’i direkt olarak alır.
    Bu event herhangi bir abone onu kanaldan alsa bile önbellekte saklanmaya devam eder.
    */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    internal fun onMyLocation(lokasyonBilgileri: EventbusDataEvents.lokasyonBilgileriniGonder){

        city = lokasyonBilgileri.City!!
        lt = lokasyonBilgileri.lt!!
        lg = lokasyonBilgileri.lg!!

        addNerden.setText(city)


    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    internal fun onIlanBilgileri(ilanBilgileri: EventbusDataEvents.ilanBilgileriniGonder){

        gelenIlan = ilanBilgileri.ilan!!

        setupIlanBilgileri()
/*
* Gerçekleştirmek istediğimiz olayı artık yakalayıp kullanmak istiyorsak da
* Fragment yada Activity sınıfımızda subscribe ederek fonksiyonu yazıyoruz
* */


    }



    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        /*
        * bir olayı yakalamak istiyorsak da o olayı yakalamak
        * istediğimizi  ilgili sınıfa haber veriyoruz bu işlemede register işlemi deniyor
        * */
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)

        /*
        * İlgili olayı artık dinlemek istemiyorsak veya onunla ilgili işlemimiz
        * bittiyse artık haberdar olmak istemediğimizi söylüyoruz
        * bu işlemi de unregister ile yapıyoruz
        * */
    }

}

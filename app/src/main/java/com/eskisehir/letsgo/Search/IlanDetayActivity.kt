package com.eskisehir.letsgo.Search

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.eskisehir.letsgo.Add.IlanDuzenleActivity
import com.eskisehir.letsgo.Home.BasvuranlarActivity
import com.eskisehir.letsgo.Model.Ilan
import com.eskisehir.letsgo.Profil.AcikProfil


import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.EventbusDataEvents
import com.eskisehir.letsgo.utils.Globals
import com.eskisehir.letsgo.utils.UniversalImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_ilan_detay.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


//sonuç sayfasından tıklanılan ilan buraya gelir

class IlanDetayActivity : AppCompatActivity(){

    var gelenIlan : Ilan? = null
    lateinit var currentUserId : String
    var isMyIlan = false
    lateinit var mRef : DatabaseReference
    var odeme = false
    var basvuru = false
    var isFinish = false
    var currentBakiye = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ilan_detay)

        mRef = FirebaseDatabase.getInstance().reference
        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid


    }


    private fun setupIlanBilgileri() {

        btnIlanDuzenle.setOnClickListener {

            val intent = Intent(this,IlanDuzenleActivity::class.java)
            EventBus.getDefault().postSticky(EventbusDataEvents.ilanBilgileriniGonder(gelenIlan))
            startActivity(intent)

        }


        ilanUserPic.setOnClickListener {
            val intent = Intent(this,AcikProfil::class.java)
            intent.putExtra("userId",gelenIlan!!.userId!!.split("&")[0])
            startActivity(intent)
        }

        // mevcut bakiyeyi anlık olarak takip eder ve değişikliklerde tetiklenerek mevcut bakiyeyi günceller
        mRef.child("users").child(gelenIlan!!.userId!!.split("&")[0]).child("bakiye").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    currentBakiye = p0.getValue(Double::class.java)!!

                }
            }

        })

        // sayfayı görüntüleyen ilan sahibi mi başvuran mı belirlenir
        if (gelenIlan!!.userId!!.split("&")[0].equals(currentUserId)){
            isMyIlan = true
        }


        // başvur butonu birden fazla fonksiyon içerir.
        // başvuran için ilana başvurmak ve ilanı geri çekmek
        // ilan veren için başvuranlara bakmak
        searchBasvur.setOnClickListener {

            //Eğer benim ilanımsa başvuranları görmek için ilgili activity başlatılır
            if (isMyIlan){

                val intent = Intent(this,BasvuranlarActivity::class.java)
                intent.putExtra("ilanId",gelenIlan!!.ilanId)
                intent.putExtra("kapasite",gelenIlan!!.kapasite)
                startActivity(intent)

            }else{

                //eğer benim ilanım değilse ve ilana başvurmuşsam burası çalışır
                if (basvuru){
                    //Başvurmuşsam statüs değerine göre buton yapılandırması olacak, satatüs verisi çekilir
                    mRef.child("ilanlar").child(gelenIlan!!.ilanId!!).child("basvuranlar").child(currentUserId).child("statüs")
                            .addListenerForSingleValueEvent(object: ValueEventListener{
                                override fun onCancelled(p0: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    val statüs = p0.getValue(Int::class.java)
                                    //statüs 3 ise ödeme yapılmış, kapasite düşürülmüş demektir. iptal edilince kapasite 1 arttırılır ve başvuru kaldırılır. Ödeme bilgisi kalmaz
                                    if (statüs == 3){
                                        mRef.child("ilanlar").child(gelenIlan!!.ilanId!!).child("kapasite")
                                                .addListenerForSingleValueEvent(object : ValueEventListener{
                                                    override fun onCancelled(p0: DatabaseError) {
                                                        TODO("Not yet implemented")
                                                    }

                                                    override fun onDataChange(p0: DataSnapshot) {
                                                        var k = p0.getValue(Int::class.java)!!
                                                        k++
                                                        mRef.child("ilanlar").child(gelenIlan!!.ilanId!!).child("kapasite").setValue(k)
                                                                .addOnCompleteListener {p0->
                                                                    if(p0.isSuccessful){
                                                                        mRef.child("ilanlar").child(gelenIlan!!.ilanId!!).child("basvuranlar").child(currentUserId).removeValue()
                                                                                .addOnCompleteListener { p0->
                                                                                    if (p0.isSuccessful){
                                                                                        Toast.makeText(this@IlanDetayActivity,"Başvuru iptal edildi",Toast.LENGTH_LONG).show()
                                                                                        basvuru = false
                                                                                    }else{
                                                                                        Toast.makeText(this@IlanDetayActivity,"Bir hata oluştu lütfen tekrar deneyiniz",Toast.LENGTH_LONG).show()
                                                                                    }
                                                                                }
                                                                        mRef.child("basvurular").child(currentUserId).child(gelenIlan!!.ilanId!!).removeValue()
                                                                    }
                                                                }
                                                    }
                                                })
                                    }else{
                                        // Eğer 3 değilse ödeme yapılmamış demektir. Bu durumda kapasite değişmediği için sadece başvuru silinir
                                        mRef.child("ilanlar").child(gelenIlan!!.ilanId!!).child("basvuranlar").child(currentUserId).removeValue()
                                                .addOnCompleteListener { p0->
                                                    if (p0.isSuccessful){
                                                        Toast.makeText(this@IlanDetayActivity,"Başvuru iptal edildi",Toast.LENGTH_LONG).show()
                                                        basvuru = false
                                                        //başvuru aynı zamanda başvurular brachından da silinir
                                                        mRef.child("basvurular").child(currentUserId).child(gelenIlan!!.ilanId!!).removeValue()


                                                    }else{
                                                        Toast.makeText(this@IlanDetayActivity,"Bir hata oluştu lütfen tekrar deneyiniz",Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                    }
                                }

                            })



                // Eğer henüz başvurmamışsam buraya gelir ve başvuru işlemleri gerçekleşir
                }else{

                    mRef.child("ilanlar").child(gelenIlan!!.ilanId!!).child("userId").addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()){
                                val isActive = p0.getValue(String::class.java)!!.split("&")[1]
                                if (isActive.equals("true")){
                                    val df = SimpleDateFormat("dd/MM/yyyy HH:mm")
                                    val sharedDate = df.format(Calendar.getInstance().time)

                                    //başvurular brancına eklemeler yapılır
                                    var basvuru=HashMap<String,Any>()
                                    basvuru.put("userId",currentUserId+"&true")
                                    basvuru.put("ilanId",gelenIlan!!.ilanId!!)
                                    mRef.child("basvurular").child(currentUserId).child(gelenIlan!!.ilanId!!).setValue(basvuru)
                                    //ilanın içerisinde de eklemeler yapılır
                                    var basvuruDetay=HashMap<String,Any>()
                                    basvuruDetay.put("basvuranId",currentUserId+"&true")
                                    basvuruDetay.put("statüs",0)
                                    basvuruDetay.put("date",sharedDate)
                                    mRef.child("ilanlar").child(gelenIlan!!.ilanId!!).child("basvuranlar").child(currentUserId).setValue(basvuruDetay)
                                            .addOnCompleteListener { p0 ->
                                                if (p0.isSuccessful){
                                                    Toast.makeText(this@IlanDetayActivity,"Başvurunuz başarıyla gerçekleştirildi",Toast.LENGTH_LONG).show()
                                                }else{
                                                    Toast.makeText(this@IlanDetayActivity,"Bir hata oluştu lütfen tekrar deneyiniz",Toast.LENGTH_LONG).show()
                                                }
                                            }
                                }else{
                                    Toast.makeText(this@IlanDetayActivity,"Bitmiş bir yolculuğa başvuru yapamazsınız.",Toast.LENGTH_LONG).show()
                                    finish()
                                }
                            }

                        }

                    })



                }



            }

        }


         odemeYap.setOnClickListener {


             if (isMyIlan){

                if (!isFinish){
                    Toast.makeText(this,"Yolculuk saati gelmeden yolculuğu bitiremezsiniz", Toast.LENGTH_LONG).show()
                }else{
                    println("aslında ödeme yapmak istiyorum: ")
                    mRef.child("ilanlar").child(gelenIlan!!.ilanId!!).child("basvuranlar").addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()){
                                var count = 0
                                var kayit = 0
                                var yolcu = 0
                                for (c in p0.children){
                                    count++
                                }

                                for (okunanUser in p0.children){
                                    val statüs = okunanUser.child("statüs").getValue(Int::class.java)

                                    if(statüs == 3){
                                        yolcu++
                                        mRef.child("users").child(okunanUser.key!!).child("rozetler").child("gezici")
                                                .addListenerForSingleValueEvent(object  : ValueEventListener{
                                                    override fun onCancelled(p0: DatabaseError) {
                                                        println(p0.message)
                                                    }

                                                    override fun onDataChange(p0: DataSnapshot) {
                                                        if (p0.exists()){
                                                            var gezici = p0.getValue(Int::class.java)!!
                                                            gezici++
                                                            mRef.child("users").child(okunanUser.key!!).child("rozetler").child("gezici").setValue(gezici)

                                                        }

                                                    }

                                                })


                                        mRef.child("basvurular").child(okunanUser.key!!).child(gelenIlan!!.ilanId!!).child("userId").setValue(okunanUser.key!!+"&false")
                                                .addOnCompleteListener { p0 ->
                                                    if (p0.isSuccessful){
                                                        var bakiye = currentBakiye + (gelenIlan!!.price!!*0.97 as Double) //şirket komisyon alıyor
                                                        mRef.child("users").child(currentUserId).child("bakiye").setValue(bakiye).addOnCompleteListener {
                                                            if (p0.isSuccessful){
                                                                kayit++
                                                                cikisYap(kayit,count,yolcu)
                                                            }
                                                        }

                                                    }
                                                }
                                    }else{
                                        mRef.child("basvurular").child(okunanUser.key!!).child(gelenIlan!!.ilanId!!).removeValue().addOnCompleteListener { p0 ->
                                            kayit++
                                            cikisYap(kayit,count,yolcu)
                                        }

                                    }
                                }
                            }else{
                                cikisYap(0,0,0)
                                println("çıkış yapasım var")
                            }
                        }

                    })

                }


             }else{
                 if (!odeme){
                     Toast.makeText(this,"Sürücü onayladıktan sonra ödeme yapabilirsiniz",Toast.LENGTH_LONG).show()
                 }else{

                     mRef.child("ilanlar").child(gelenIlan!!.ilanId!!).child("basvuranlar").child(currentUserId).child("statüs").setValue(3)
                             .addOnCompleteListener { p0 ->
                                 if (p0.isSuccessful){
                                     Toast.makeText(this,"Ödeme yapildi",Toast.LENGTH_LONG).show()
                                     mRef.child("ilanlar").child(gelenIlan!!.ilanId!!).child("kapasite")
                                             .addListenerForSingleValueEvent(object : ValueEventListener{
                                                 override fun onCancelled(p0: DatabaseError) {
                                                     TODO("Not yet implemented")
                                                 }

                                                 override fun onDataChange(p0: DataSnapshot) {
                                                     var k = p0.getValue(Int::class.java)!!
                                                     k--
                                                     mRef.child("ilanlar").child(gelenIlan!!.ilanId!!).child("kapasite").setValue(k)
                                                             .addOnCompleteListener {  }

                                                 }

                                             })
                                 }else{
                                     Toast.makeText(this,"Bir hata oluştu lütfen tekrar deneyiniz",Toast.LENGTH_LONG).show()
                                 }
                             }



                 }
             }


        }


        if (isMyIlan){

            searchBasvur.text = "Başvuranlara bak"
            odemeYap.text = "Yolculuğu Bitir"
            btnIlanDuzenle.visibility = View.VISIBLE
            val currentUnix = System.currentTimeMillis() / 1000L;
            if (currentUnix > gelenIlan!!.unix!!){
                odemeYap.setBackgroundResource(R.color.yesil)
                odemeYap.setTextColor(ContextCompat.getColor(this,R.color.beyaz))
                isFinish = true
            }else{
                isFinish = false
            }

        }else{
            btnIlanDuzenle.visibility = View.GONE
            mRef.child("ilanlar").child(gelenIlan!!.ilanId!!).child("basvuranlar").child(currentUserId).child("statüs")
                    .addValueEventListener(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()){
                                basvuru = true
                                searchBasvur.setBackgroundResource(R.drawable.button_inactive)
                                searchBasvur.setTextColor(ContextCompat.getColor(this@IlanDetayActivity,R.color.kirmizi))
                                searchBasvur.text = "Başvuruyu İptal Et"

                                val flag = p0.getValue(Int::class.java)!!
                                if (flag == 2){
                                    odemeYap.setBackgroundResource(R.drawable.button_active)
                                    odemeYap.setTextColor(ContextCompat.getColor(this@IlanDetayActivity,R.color.beyaz))
                                    odeme = true
                                }else{
                                    odemeYap.setBackgroundResource(R.drawable.button_inactive)
                                    odemeYap.setTextColor(ContextCompat.getColor(this@IlanDetayActivity,R.color.mavi))
                                    odeme = false
                                }

                            }else{
                                basvuru = false
                                searchBasvur.setBackgroundResource(R.drawable.button_active)
                                searchBasvur.setTextColor(ContextCompat.getColor(this@IlanDetayActivity,R.color.beyaz))
                                searchBasvur.text = "Başvur"

                                odemeYap.setBackgroundResource(R.drawable.button_inactive)
                                odemeYap.setTextColor(ContextCompat.getColor(this@IlanDetayActivity,R.color.mavi))
                                odeme = false

                            }
                        }

                    })





        }

        mRef.child("users").child(gelenIlan!!.userId!!.split("&")[0]).child("profile_picture")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            val photoUrl = p0.getValue(String::class.java)!!
                            UniversalImageLoader.setImage(photoUrl,ilanUserPic,null,"")
                        }

                    }

                })

        mRef.child("users").child(gelenIlan!!.userId!!.split("&")[0]).child("adi_soyadi")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                          ilanAdiSoyadi.text = p0.getValue(String::class.java)!!

                        }

                    }

                })

        ilanNerden.text = gelenIlan!!.rotaDate!!.split("&")[0]
        ilanNereye.text = gelenIlan!!.rotaDate!!.split("&")[1]
        ilanDate.text = gelenIlan!!.rotaDate!!.split("&")[2]
        ilanFiyat.text = gelenIlan!!.price.toString()
        ilanSaat.text = gelenIlan!!.saat
        ilanBosKoltuk.text = ""+gelenIlan!!.kapasite + " koltuk boş"




    }

    private fun cikisYap(kayit: Int, count: Int, yolcu: Int) {

        if (yolcu>0){
            mRef.child("users").child(currentUserId).child("rozetler").child("sofor")
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()){
                                var sofor = p0.getValue(Int::class.java)!!
                                sofor++
                                mRef.child("users").child(currentUserId).child("rozetler").child("sofor").setValue(sofor)
                            }
                        }

                    })
            println("kayıt: "+kayit+" count: "+count)
            if (kayit == count){
                mRef.child("ilanlar").child(gelenIlan!!.ilanId!!).child("userId").setValue(currentUserId+"&false").addOnCompleteListener { p0 ->
                    if (p0.isSuccessful){
                        Toast.makeText(this,"Yolculuk başarıyla sona erdi.Bakiyeniz güncellendi. Yolcuları değerlendirmek için profilinize gidiniz.",Toast.LENGTH_LONG).show()
                        val gl = Globals.letsGo
                        gl.returnHome()!!.reload()
                        onBackPressed()
                    }

                }

            }
    }else{
            mRef.child("ilanlar").child(gelenIlan!!.ilanId!!).removeValue(object : DatabaseReference.CompletionListener{
                override fun onComplete(p0: DatabaseError?, p1: DatabaseReference) {
                    Toast.makeText(this@IlanDetayActivity,"Hiç yolcunuz olmadığı için ilan silindi",Toast.LENGTH_LONG).show()
                    val gl = Globals.letsGo
                    gl.returnHome()!!.reload()
                    onBackPressed()
                }


            })


        }


    }


    @Subscribe(sticky = true)
    internal fun onMode(mode: EventbusDataEvents.modeGonder){

        val gl = Globals.letsGo
        gl.returnHome()!!.reload()
        finish()

    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    internal fun onIlanBilgileri(ilanBilgileri: EventbusDataEvents.ilanBilgileriniGonder){

        gelenIlan = ilanBilgileri.ilan

        setupIlanBilgileri()



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

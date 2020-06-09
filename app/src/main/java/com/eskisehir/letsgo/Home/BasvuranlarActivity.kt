package com.eskisehir.letsgo.Home


import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.eskisehir.letsgo.Model.Basvuranlar
import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.BasvuranAdapter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_basvuranlar.*


//ilan sahibi için başvuranları görmek için eklenmiş sayfa


class BasvuranlarActivity : AppCompatActivity() {

    var basvuranlar:ArrayList<Basvuranlar> = ArrayList()
    lateinit var mRef : DatabaseReference
    var kapasite = 0
    var guncelKapasite = 0
    lateinit var basvuranOnay : Button
    var ilanId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basvuranlar)

        val intent = intent
        ilanId = intent.getStringExtra("ilanId")//key değeri ilanId olan veriyi aldık
        kapasite = intent.getIntExtra("kapasite",0)
        guncelKapasite = kapasite
        println(ilanId)
        mRef = FirebaseDatabase.getInstance().reference
        basvuranOnay = findViewById(R.id.basvuranOnay)//butonu aktiviteye bağlama işlemi

        //gelen ilanın id bilgisine göre başvuranlar veritabanından çekilir
        mRef.child("ilanlar").child(ilanId).child("basvuranlar")//ilanId'yi getStringExtra ile almıştık
                .addValueEventListener(object : ValueEventListener{  //To read data at a path and listen for changes, use the addValueEventListener()
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            basvuranlar.clear()
                            tvBasvuranlar.visibility = View.GONE //"ilana henüz basvuru yapılmadı" yazısı yok olur
                            rvBasvuranlar.visibility = View.VISIBLE //basvuranlar listelenir
                            basvuranOnay.visibility = View.VISIBLE //onay butonu aktif olur

                            //başvuranın durumuna göre liste oluşturulur
                            for (user in p0.children){
                                var userId = user.key!!
                                var onay = user.child("statüs").getValue(Int::class.java)!!
                                var p = Basvuranlar(userId,onay)
                                basvuranlar.add(p)
                            }

                            rvBasvuranlar.layoutManager = LinearLayoutManager(this@BasvuranlarActivity)
                            var adapter = BasvuranAdapter(this@BasvuranlarActivity,basvuranlar,kapasite)
                            rvBasvuranlar.adapter = adapter

                        }else{
                            tvBasvuranlar.visibility = View.VISIBLE //"ilana henüz basvuru yapılmadı" yazısı gözükür
                            rvBasvuranlar.visibility = View.GONE
                            basvuranOnay.visibility = View.GONE //eğer ilana başvuru yapılmamışsa onay butonu görünmez
                        }
                    }

                })


        //listedeki checkboxlar adapter içinde kodlandı
        //onay kısmında seçilen kullanıcıların statüsleri ödeme yapmalarına olanak verecek hale getirilir
        basvuranOnay.setOnClickListener {


            for (basvuru in basvuranlar){
               if (basvuru.onay == 1){
                   mRef.child("ilanlar").child(ilanId).child("basvuranlar").child(basvuru.userId).child("statüs").setValue(2)
                           .addOnCompleteListener { p0 ->
                               if (p0.isSuccessful){
                                    kapasite--
                                   if (guncelKapasite == kapasite){
                                       Toast.makeText(this,"Başvurular onaylandı",Toast.LENGTH_LONG).show()
                                       onBackPressed()
                                   }
                               }

                           }
               }

           }

        }






    }
}

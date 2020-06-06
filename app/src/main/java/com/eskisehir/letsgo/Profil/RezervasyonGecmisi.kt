package com.eskisehir.letsgo.Profil

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.TransferGecmisiAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_rezervasyon_gecmisi.*

class RezervasyonGecmisi : AppCompatActivity() {

    lateinit var mRef : DatabaseReference
    lateinit var currentUserId : String
    val ilanlar : ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rezervasyon_gecmisi)

        mRef = FirebaseDatabase.getInstance().reference
        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

        init()

    }

    private fun init() {


    //başvurular child i içerisinden current user başvuruları çekilir ve başvuru idleri adaptere gönderilir. adapter içerinde ilan detaylarına erişilir
        mRef.child("basvurular").child(currentUserId)
                .addValueEventListener(object  : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        println("Debug 3 ")
                        if (p0.exists()){
                            println("Debug 4 ")
                            for (basvuru in p0.children){
                                println("Debug 5 ")
                                val ilanId = basvuru.child("ilanId").getValue(String::class.java)
                                val userId = basvuru.child("userId").getValue(String::class.java)
                                if (userId!!.split("&")[1].equals("false")){
                                    ilanlar.add(ilanId!!)
                                    println(ilanId)
                                }

                            }
                            println("Debug 6 ")
                            rvGecmisTransfer.layoutManager = LinearLayoutManager(this@RezervasyonGecmisi)
                            val adapter = TransferGecmisiAdapter(this@RezervasyonGecmisi,ilanlar)
                            rvGecmisTransfer.adapter = adapter
                            println("Debug 7 ")

                        }


                    }

                })

    }
}

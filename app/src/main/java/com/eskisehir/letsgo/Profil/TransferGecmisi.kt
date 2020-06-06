package com.eskisehir.letsgo.Profil

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.TransferGecmisiAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_transfer_gecmisi.*

class TransferGecmisi : AppCompatActivity() {

    lateinit var mRef : DatabaseReference
    lateinit var currentUserId : String
    val ilanlar : ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_gecmisi)

        mRef = FirebaseDatabase.getInstance().reference
        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

        println("Debug 1 ")
        init()


    }

    private fun init() {

        //false olan değerler geçmiş transferlerdir. ilanlarda bulunan kullanıcılar taşınmış demektir. yolculara böyle ulaşılır
        //idler alınır ve adaptere atılır. user bilgileri orada çekilir
        mRef.child("ilanlar").orderByChild("userId").equalTo(currentUserId+"&false")
                .addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            for (ilan in p0.children){
                                val ilanId = ilan.key!!
                                ilanlar.add(ilanId)
                            }

                            rvGecmisTransfer.layoutManager = LinearLayoutManager(this@TransferGecmisi)
                            val adapter = TransferGecmisiAdapter(this@TransferGecmisi,ilanlar)
                            rvGecmisTransfer.adapter = adapter


                        }
                    }

                })

    }
}

package com.example.letsgoo.Profil

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.YolcuPuanlamaAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_verilen_puanlar.*

class VerilenPuanlar : AppCompatActivity() {


    lateinit var mRef : DatabaseReference
    lateinit var currentUserId : String
    val users : ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verilen_puanlar)

        mRef = FirebaseDatabase.getInstance().reference
        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

        //kişinin kendi branchında bu bilgi mevcuttur. Dallanma ile oraya gidilir veriler çekilerek adaptere atılır
        mRef.child("users").child(currentUserId).child("puanlar").child("verilenPuan")
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            for (user in p0.children){
                                users.add(user.key!!)

                            }

                            rvVerilenPuanlar.layoutManager = LinearLayoutManager(this@VerilenPuanlar)
                            val adapter = YolcuPuanlamaAdapter(this@VerilenPuanlar,users)
                            rvVerilenPuanlar.adapter = adapter

                        }
                    }

                })


    }
}

package com.example.letsgoo.Profil

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsgoo.R
import com.example.letsgoo.utils.YolcuPuanlamaAdapter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_yolcu_puanlama.*


class YolcuPuanlamaActivity : AppCompatActivity() {

    lateinit var mRef: DatabaseReference
    val users : ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yolcu_puanlama)

        mRef = FirebaseDatabase.getInstance().reference

        val intent = intent
        val ilanId = intent.getStringExtra("ilanId")



        //tüm başvuranlar çekilir ve adaptere gönderilir gerekli kontoller orada yapılır
        mRef.child("ilanlar").child(ilanId).child("basvuranlar").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    for (user in p0.children){
                        users.add(user.key!!)
                    }

                    rvYolcuPuanlama.layoutManager = LinearLayoutManager(this@YolcuPuanlamaActivity)
                    val adapter = YolcuPuanlamaAdapter(this@YolcuPuanlamaActivity,users)
                    rvYolcuPuanlama.adapter = adapter


                }
            }

        })





    }
}

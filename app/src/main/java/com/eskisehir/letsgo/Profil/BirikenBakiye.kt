package com.eskisehir.letsgo.Profil

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.eskisehir.letsgo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_biriken_bakiye.*


//sürücü için yaptığı transferlerden sonra kazandığı parayı çekeceği sayfayı içerir

class BirikenBakiye : AppCompatActivity() {

    var bakiye = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biriken_bakiye)

        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("bakiye").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    bakiye = p0.getValue(Int::class.java)!!
                    tvBakiye.text =     ""+bakiye + " TL"

                    //bakiye 100 den büyükse para çekebilir. Küçükse buton inaktif yapılır.
                    if (bakiye > 100){
                        paraCek.setBackgroundResource(R.drawable.button_active)
                        paraCek.setTextColor(ContextCompat.getColor(this@BirikenBakiye,R.color.beyaz))
                    }else{
                        paraCek.setBackgroundResource(R.drawable.button_inactive)
                        paraCek.setTextColor(ContextCompat.getColor(this@BirikenBakiye,R.color.mavi))
                    }
                }
            }
        })


        paraCek.setOnClickListener {

            //para çekilince veritabanı güncellenir
            if (bakiye > 100){
                FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("bakiye").setValue(0)
                        .addOnCompleteListener { p0 ->
                            if (p0.isSuccessful){
                                Toast.makeText(this,"Paranız hesabınıza yüklenmiştir",Toast.LENGTH_LONG).show()
                            }
                        }
            }else{
                Toast.makeText(this,"Para çekebilmek için bakiyenin 100 tl üzerinde olması gerekir",Toast.LENGTH_LONG).show()
            }



        }


    }
}

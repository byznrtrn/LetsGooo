package com.eskisehir.letsgo.Home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.eskisehir.letsgo.Admin.AdminActivity
import com.eskisehir.letsgo.Login.FirstActivity
import com.eskisehir.letsgo.Model.Users
import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.BottomNavigationViewHelper
import com.eskisehir.letsgo.utils.UniversalImageLoader
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.tvAski



//main aktivity bizim tüm fragmentlerimizi barından kısım.
class MainActivity : AppCompatActivity() {


    var active = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

        //giriş yapılınca kullanıcı admin mi diye bakılır
        if (FirebaseAuth.getInstance().currentUser!!.email.equals("admin@letsgo.com")){
            startActivity(Intent(this,AdminActivity::class.java))
            finish()
        }else{
            //admin değilse hesap askıya alınmış mı diye bakılır
            FirebaseDatabase.getInstance().reference.child("users").child(currentUserId).addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    println(p0.message)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val user = p0.getValue(Users::class.java)!!
                        if (user.statüs == 0){//status 0sa engellenmiş demektir
                            Toast.makeText(this@MainActivity,"Üzgünüz Hesabınız Askıya Alınmıştır",Toast.LENGTH_LONG).show()
                            inactiveLayout.visibility = View.VISIBLE
                            activeLayout.visibility = View.GONE
                            tvAski.text = "Hesabınız askıya alınmıştır.\n" +
                                    "Eğer bir hata oluğunu düşünüyorsanız \n" +
                                    "admin@letsgo.com E-Maili ile iletişime geçebilirsiniz"
                        }else{
                            activeLayout.visibility = View.VISIBLE
                            inactiveLayout.visibility = View.GONE
                        }

                    }
                }

            })

        }

        tvCikisYap.setOnClickListener{
            FirebaseAuth.getInstance().signOut()

            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener {
                        startActivity(Intent(this, FirstActivity::class.java))
                        finish()
                    }
        }

        setupNavigationView()
        initImageLoader()

    }


    //taban kısımdaki navigasyon için kurulum kısmı. Utils bölümündeki class ile sağlanıyor
    fun setupNavigationView(){
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView)
        BottomNavigationViewHelper.setupNavigation(this,bottomNavigationView)

    }

    //fotoğraf yüklemeleri için tanımlama
    private fun initImageLoader(){
        val universalImageLoader = UniversalImageLoader(this)
        ImageLoader.getInstance().init(universalImageLoader.config)
    }

    @Override
      override fun onStart() {
         super.onStart();
         active = true;
      }

      @Override
      override fun onStop() {
         super.onStop();
         active = false;
      }


}

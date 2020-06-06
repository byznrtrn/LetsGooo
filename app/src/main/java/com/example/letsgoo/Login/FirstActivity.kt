package com.example.letsgoo.Login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.letsgoo.Home.MainActivity
import com.example.letsgoo.R
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_first.*

//uygulama ilk açıldığında açılan uyguşlama
class FirstActivity : AppCompatActivity() {
//lateinit:sonradan tanımlayacağımızı belirtiyoruz...ayrıca başka yerde başlatılana kadar bellekte yer tutmaz
    lateinit var mAuth : FirebaseAuth
    lateinit var mAuthListener:FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        mAuth = FirebaseAuth.getInstance()
       // mAuth.signOut()
        //AuthUI.getInstance().signOut(this)
        setupAuthListener()

        //giriş ya da kayıt durumuna göre ilgili sayfaya yönlendirme yapılıyor

        btnGirisYap.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnKaydol.setOnClickListener {
            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun setupAuthListener() {
        mAuthListener = object : FirebaseAuth.AuthStateListener{
            override fun onAuthStateChanged(p0: FirebaseAuth) {

                var user = FirebaseAuth.getInstance().currentUser
                if (user != null){
                    val intent = Intent(this@FirstActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    finish()
                }else{

                }

            }

        }
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onStop() {
        super.onStop()
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener)
        }
    }



}

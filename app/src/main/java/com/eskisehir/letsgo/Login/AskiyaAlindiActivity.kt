package com.eskisehir.letsgo.Login


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eskisehir.letsgo.R
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_askiya_alindi.*


//bir hesap askıya alındıysa buraya gelir ve çıkış yapma fonksiyonuna sahip olabilir
class AskiyaAlindiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_askiya_alindi)

        tvAski.text = "Hesabınız askıya alınmıştır.\n" +
                "Eğer bir hata oluğunu düşünüyorsanız \n" +
                "admin@letsgo.com E-Maili ile iletişime geçebilirsiniz"


        tvCikisYap.setOnClickListener{
            FirebaseAuth.getInstance().signOut()

            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener {
                        startActivity(Intent(this, FirstActivity::class.java))
                        finish()
                    }
        }

    }
}

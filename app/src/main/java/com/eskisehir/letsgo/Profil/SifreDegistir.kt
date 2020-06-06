package com.eskisehir.letsgo.Profil

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.eskisehir.letsgo.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sifre_degistir.*

//şifre değilimi için basit bi mantık var. Eski şifre doğru mu diye bi daha giriş yapılır doğruysa eski yeni şifreler eşit mi diye bakılır ve değişiklik kaydedilir
class SifreDegistir : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sifre_degistir)


        imgKapat.setOnClickListener {
            onBackPressed()
        }

        imgSifreDegistir.setOnClickListener {

            var mevcutSifre=etMevcutSifre!!.text!!.toString()
            var yeniSifre=etYeniSifre!!.text!!.toString()
            var yeniSifreTekrar=eetYeniSifreTekrar!!.text!!.toString()


            if(!mevcutSifre.isNullOrEmpty() && mevcutSifre.length>=6){

                var myUser= FirebaseAuth.getInstance().currentUser
                if(myUser != null){
                    var credential= EmailAuthProvider.getCredential(myUser!!.email.toString(),mevcutSifre)
                    myUser.reauthenticate(credential).addOnCompleteListener(object : OnCompleteListener<Void> {
                        override fun onComplete(p0: Task<Void>) {
                            if(p0!!.isSuccessful){

                                if(yeniSifre.equals(yeniSifreTekrar)){

                                    if(!yeniSifre.isNullOrEmpty() && yeniSifre.length>=6){

                                        var myUser=FirebaseAuth.getInstance().currentUser
                                        myUser!!.updatePassword(yeniSifre).addOnCompleteListener(object : OnCompleteListener<Void>{
                                            override fun onComplete(p0: Task<Void>) {
                                                if(p0!!.isSuccessful){
                                                    Toast.makeText(this@SifreDegistir,"Şifreniz güncellendi",
                                                        Toast.LENGTH_SHORT).show()
                                                }else {
                                                    Toast.makeText(this@SifreDegistir,"Şifre güncellenemedi",
                                                        Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                        })


                                    }else {
                                        Toast.makeText(this@SifreDegistir,"Yeni şifre en az 6 karakter olmalıdır",
                                            Toast.LENGTH_SHORT).show()
                                    }

                                }else {
                                    Toast.makeText(this@SifreDegistir,"Şifreler eşleşmiyor", Toast.LENGTH_SHORT).show()
                                }



                            }else {
                                Toast.makeText(this@SifreDegistir,"Mevcut şifreniz yanlış", Toast.LENGTH_SHORT).show()
                            }
                        }

                    })
                }


            }else {
                Toast.makeText(this,"Mevcut şifre en az 6 karakter olmalıdır", Toast.LENGTH_SHORT).show()
            }


        }

    }
}

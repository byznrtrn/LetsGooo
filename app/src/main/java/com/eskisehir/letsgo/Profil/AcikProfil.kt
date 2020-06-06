package com.eskisehir.letsgo.Profil

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.eskisehir.letsgo.Model.Users
import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.UniversalImageLoader
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_acik_profil.*
import java.util.*


//başkalarının profillerini ve bizim herkese açık profili görüntülemek için bu sayfa kullanılır
class AcikProfil : AppCompatActivity() {

    lateinit var userId: String
    lateinit var okunanKullaniciBilgileri: Users
    lateinit var mRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acik_profil)

        val intent = intent
        userId = intent.getStringExtra("userId")
        mRef = FirebaseDatabase.getInstance().reference


        //kişi bilgileri gelen id ye göre çekilir

        mRef.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener{ //use addListenerForSingleValueEvent() method to add a ValueEventListener to a DatabaseReference
            override fun onCancelled(p0: DatabaseError) {
                println(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {//onDataChange metodu database'te her değişiklik olduğunda çağırılır.
                if (p0.exists()){
                    okunanKullaniciBilgileri = p0.getValue(Users::class.java)!!
                    //çekilen bilgilere göre aşağıdaki bilgiler yerleştirilir
                    setupTercihler()
                    setupRozetler()
                    setupProfil()
                }
            }

        })


    }
//profilbilgileri eklenir
    private fun setupProfil() {


    //genel kişisel bilgilerin eklendiği kısım
        UniversalImageLoader.setImage(okunanKullaniciBilgileri.profile_picture!!,profileUserPic!!,progressBar,"")
        profilNameSurname.text = okunanKullaniciBilgileri.adi_soyadi
        if (!okunanKullaniciBilgileri.dogumYili.equals("")){
            var year = okunanKullaniciBilgileri.dogumYili!!.toInt()
            var currentYear = Calendar.getInstance().get(Calendar.YEAR);
            var yas = currentYear - year
            tvYas.text = ""+yas+" Yaşında"
        }else{
            tvYas.text = "Yaş Bilinmiyor"
        }

        mRef.child("users").child(userId).child("puanlar").child("alinanPuan")
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        println(p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            var puanSayisi = 0
                            var puanToplamı = 0
                            for (puan in p0.children){
                                puanSayisi++
                                puanToplamı += puan.getValue(Int::class.java)!!
                            }
                            tvPuanSayisi.text = "($puanSayisi)"
                            var ort = puanToplamı.toDouble() / puanSayisi
                            ratingBar.rating = ort.toFloat()

                        }else{
                            tvPuanSayisi.text ="Henüz Puan Verilmemiş"
                        }
                    }

                })


    }


    //rozetlerin eklendiği kısım
    private fun setupRozetler() {
        var rozetCount = 0
        val rozet = okunanKullaniciBilgileri.rozetler!!
        //eğitim rozeti kayıt sırasın belirlenir ve .edu varsa verilir
        if (rozet.egitim == 1){
            rozetCount++
            imgEdu.visibility = View.VISIBLE
        }else{
            imgEdu.visibility = View.GONE
        }

        //güvenilir rozeti için 5 defa tam yıldız almak gerekir. Tam yıldızlar burada sayılır ve sayısına göre rozet verilir
        if (rozet.guvenilir > 5){
            rozetCount++
            imgGuvenilir.visibility = View.VISIBLE
        }else{
            imgGuvenilir.visibility = View.GONE
        }
//gezici rozeti için 5 yolculuk yapmak gerekir
        if (rozet.gezici > 5){
            rozetCount++
            imgGezici.visibility = View.VISIBLE
        }else{
            imgGezici.visibility = View.GONE
        }
//bu rozet için 5 defa yolcu taşımak gerekir
        if (rozet.sofor > 5){
            rozetCount++
            imgSofor.visibility = View.VISIBLE
        }else{
            imgSofor.visibility = View.GONE
        }
//5 defa puanlama yapınca bu rozet alınır
        if (rozet.gozlemci > 5){
            rozetCount++
            imgGozlemci.visibility = View.VISIBLE
        }else{
            imgGozlemci.visibility = View.GONE
        }

        //rozet yoksa rozet kısmı görünmez yapılır
        if(rozetCount == 0){
            tvRozetler.visibility = View.GONE
        }



    }

    //tercihler çekilen verilere göre yerleştirilir
    //0-> bilinmiyor
    //1-> pozitif karar
    //-1-> negatif karar
    private fun setupTercihler() {
        if (okunanKullaniciBilgileri.tercihler!!.talk!! == 0){
            editTercihlerImgTalk.setImageResource(R.drawable.question)
            editTercihlerTalk.setText(R.string.bilinmiyor)
        }else if (okunanKullaniciBilgileri.tercihler!!.talk!! == 1){
            editTercihlerImgTalk.setImageResource(R.drawable.talkative)
            editTercihlerTalk.setText(R.string.talk)
        }else{
            editTercihlerImgTalk.setImageResource(R.drawable.no_talkative)
            editTercihlerTalk.setText(R.string.no_talk)
        }

        if (okunanKullaniciBilgileri.tercihler!!.smoke!! == 0){
            editTercihlerImgSmoke.setImageResource(R.drawable.question)
            editTercihlerSmoke.setText(R.string.bilinmiyor)
        }else if (okunanKullaniciBilgileri.tercihler!!.smoke!! == 1){
            editTercihlerImgSmoke.setImageResource(R.drawable.smoke)
            editTercihlerSmoke.setText(R.string.smoke)
        }else{
            editTercihlerImgSmoke.setImageResource(R.drawable.no_smoke)
            editTercihlerSmoke.setText(R.string.no_smoke)
        }

        if (okunanKullaniciBilgileri.tercihler!!.music!! == 0){
            editTercihlerImgMusic.setImageResource(R.drawable.question)
            editTercihlerMusic.setText(R.string.bilinmiyor)
        }else if (okunanKullaniciBilgileri.tercihler!!.music!! == 1){
            editTercihlerImgMusic.setImageResource(R.drawable.music)
            editTercihlerMusic.setText(R.string.music)
        }else{
            editTercihlerImgMusic.setImageResource(R.drawable.no_music)
            editTercihlerMusic.setText(R.string.no_music)
        }

        if (okunanKullaniciBilgileri.tercihler!!.pet!! == 0){
            editTercihlerImgPet.setImageResource(R.drawable.question)
            editTercihlerPet.setText(R.string.bilinmiyor)
        }else if (okunanKullaniciBilgileri.tercihler!!.pet!! == 1){
            editTercihlerImgPet.setImageResource(R.drawable.pet)
            editTercihlerPet.setText(R.string.pet)
        }else{
            editTercihlerImgPet.setImageResource(R.drawable.no_pet)
            editTercihlerPet.setText(R.string.no_pet)
        }

    }

}

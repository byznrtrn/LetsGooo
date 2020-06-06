package com.eskisehir.letsgo.Profil

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import com.eskisehir.letsgo.Model.Tercihler
import com.eskisehir.letsgo.Model.Users
import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.EventbusDataEvents
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_tercihler.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class Tercihler : AppCompatActivity() {


    var talk:Int = 0
    var smoke:Int = 0
    var music:Int = 0
    var pet:Int = 0
    lateinit var okunanKullaniciBilgileri: Users
    var tercihler : Tercihler? = null
    lateinit var mRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tercihler)

        mRef = FirebaseDatabase.getInstance().reference


        //değişiklikler kaydedilir
        editTercihlerKaydet.setOnClickListener {

            val tercihler:Tercihler = Tercihler(talk,smoke,music,pet)
            mRef.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("tercihler").setValue(tercihler)
                .addOnCompleteListener { p0 ->
                    if (p0.isSuccessful){
                        Toast.makeText(this,"Tercihler Kaydedildi",Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }else{
                        Toast.makeText(this,"Bir hata oluştu lütfen daha sonra tekrar deneyiniz",Toast.LENGTH_SHORT).show()
                    }
                }




        }

    }





    private fun setupChange() {

        //her kısım için tıklamaya göre çıkacak popuplar eklenir
        editTercihlerTalk.setOnClickListener {
            val popup : PopupMenu = PopupMenu(this,editTercihlerTalk)
            popup.inflate(R.menu.tercihler_talk_menu)
            val et = editTercihlerTalk
            val eti = editTercihlerImgTalk
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when(item!!.itemId){
                        R.id.TercihlerTalk ->{
                            et.setText(R.string.talk)
                            eti.setImageResource(R.drawable.talkative)
                            talk = 1
                            return true

                        }

                        R.id.TercihlerTalkBilinmiyor-> {
                            et.setText(R.string.bilinmiyor)
                            eti.setImageResource(R.drawable.question)
                            talk = 0
                            return true
                        }

                        R.id.TercihlerNoTalk-> {
                            et.setText(R.string.no_talk)
                            eti.setImageResource(R.drawable.no_talkative)
                            talk = -1
                            return true
                        }
                    }
                    return false
                }

            })

            popup.show()
        }
        editTercihlerSmoke.setOnClickListener {
            val popup : PopupMenu = PopupMenu(this,editTercihlerSmoke)
            popup.inflate(R.menu.tercihler_smoke_menu)
            val et = editTercihlerSmoke
            val eti = editTercihlerImgSmoke
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when(item!!.itemId){
                        R.id.TercihlerSmoke ->{
                            et.setText(R.string.smoke)
                            eti.setImageResource(R.drawable.smoke)
                            smoke = 1
                            return true

                        }

                        R.id.TercihlerSmokeBilinmiyor-> {
                            et.setText(R.string.bilinmiyor)
                            eti.setImageResource(R.drawable.question)
                            smoke = 0
                            return true
                        }

                        R.id.TercihlerNoSmoke-> {
                            et.setText(R.string.no_smoke)
                            eti.setImageResource(R.drawable.no_smoke)
                            smoke = -1
                            return true
                        }
                    }
                    return false
                }

            })

            popup.show()
        }
        editTercihlerMusic.setOnClickListener {
            val popup : PopupMenu = PopupMenu(this,editTercihlerMusic)
            popup.inflate(R.menu.tercihler_music_menu)
            val et = editTercihlerMusic
            val eti = editTercihlerImgMusic
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when(item!!.itemId){
                        R.id.TercihlerMusic ->{
                            et.setText(R.string.music)
                            eti.setImageResource(R.drawable.music)
                            music=1
                            return true

                        }

                        R.id.TercihlerTalkBilinmiyor-> {
                            et.setText(R.string.bilinmiyor)
                            eti.setImageResource(R.drawable.question)
                            music = 0
                            return true
                        }

                        R.id.TercihlerNoMusic-> {
                            et.setText(R.string.no_music)
                            eti.setImageResource(R.drawable.no_music)
                            music = -1
                            return true
                        }
                    }
                    return false
                }

            })

            popup.show()
        }
        editTercihlerPet.setOnClickListener {
            val popup : PopupMenu = PopupMenu(this,editTercihlerPet)
            popup.inflate(R.menu.tercihler_pet_menu)
            val et = editTercihlerPet
            val eti = editTercihlerImgPet
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when(item!!.itemId){
                        R.id.TercihlerPet ->{
                            et.setText(R.string.pet)
                            eti.setImageResource(R.drawable.pet)
                            pet = 1
                            return true

                        }

                        R.id.TercihlerTalkBilinmiyor-> {
                            et.setText(R.string.bilinmiyor)
                            eti.setImageResource(R.drawable.question)
                            pet = 0
                            return true
                        }

                        R.id.TercihlerNoPet-> {
                            et.setText(R.string.no_pet)
                            eti.setImageResource(R.drawable.no_pet)
                            pet = -1
                            return true
                        }
                    }
                    return false
                }

            })

            popup.show()
        }
    }


    //tercihler kayıtlı olduğu halde ekrana yerleştirilir
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

    @Subscribe(sticky = true)
    internal fun onTercihler(kullaniciBilgileri: EventbusDataEvents.KullaniciBilgileriniGonder){

        okunanKullaniciBilgileri = kullaniciBilgileri.kullanici!!
        var tercihler = okunanKullaniciBilgileri.tercihler

        talk = tercihler!!.talk!!
        smoke = tercihler!!.smoke!!
        music = tercihler!!.music!!
        pet = tercihler!!.pet!!
        setupTercihler()

        setupChange()


    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}

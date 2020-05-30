package com.example.letsgoo.Profil


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.example.letsgoo.Model.Users

import com.example.letsgoo.R
import com.example.letsgoo.utils.EventbusDataEvents
import com.example.letsgoo.utils.UniversalImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.fragment_my_info.view.*
import org.greenrobot.eventbus.EventBus


/**
 * Profilde bulunan iki sayfadan biri. Araba tercihler vb bilgileri içerir
 */
class MyInfoFragment : Fragment() {

    var ozgecmis:String = ""
    lateinit var mUser: FirebaseUser
    lateinit var mAuth : FirebaseAuth
    lateinit var mRef : DatabaseReference
    lateinit var okunanKullaniciBilgileri:Users
    lateinit var mView:View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_my_info, container, false)
        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser!!
        mRef = FirebaseDatabase.getInstance().reference

        //ayarlara basınca iki seçenek çıkar ve ona göre tercihler yada kişisel bilgiler güncellenir
        mView.profileTercihlerAyarlar.setOnClickListener {


            val popup : PopupMenu = PopupMenu(context,mView.profileTercihlerAyarlar)
            popup.inflate(R.menu.profile_hakkinda_menu)
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when(item!!.itemId){
                        R.id.profile_tercihler ->{

                        val intent = Intent(activity!!,Tercihler::class.java)
                            EventBus.getDefault().postSticky(EventbusDataEvents.KullaniciBilgileriniGonder(okunanKullaniciBilgileri))
                            startActivity(intent)
                            return true

                        }

                        R.id.profile_hakkinda -> {

                            val intent = Intent(activity!!,KisiselBilgiler::class.java)
                            EventBus.getDefault().postSticky(EventbusDataEvents.KullaniciBilgileriniGonder(okunanKullaniciBilgileri))
                            startActivity(intent)
                            return true

                        }
                    }

                    return false
                }

            })

            popup.show()


        }

        //araba ekleme de düzenle dersek var olan arabayı düzenler. ekle dersek araba ekler.
        mView.profileArabaEkle.setOnClickListener {


            val popup : PopupMenu = PopupMenu(context,mView.profileArabaEkle)
            popup.inflate(R.menu.profile_araba_ekle)
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when(item!!.itemId){
                        R.id.profile_araba_ekle ->{

                            val intent = Intent(activity!!,AddCar::class.java)
                            EventBus.getDefault().postSticky(EventbusDataEvents.arabaBilgileriniGonder(okunanKullaniciBilgileri.car,"new"))
                            startActivity(intent)
                            return true

                        }

                        R.id.profile_araba_duzenle-> {

                            val intent = Intent(activity!!,AddCar::class.java)
                            EventBus.getDefault().postSticky(EventbusDataEvents.arabaBilgileriniGonder(okunanKullaniciBilgileri.car,"old"))
                            startActivity(intent)
                            return true

                        }
                    }

                    return false
                }

            })

            popup.show()


        }

        //her kese açık profili görecek aktivitye gider
        mView.profiliniGor.setOnClickListener {
            val intent = Intent(activity!!,AcikProfil::class.java)
            intent.putExtra("userId",mUser.uid)
            startActivity(intent)
        }



        kullaniciBilgileriniGetir()




        return mView
    }

    //özgeçmiş bu sayfada gösterilir. eğer değişiklik varsa buraya yansır
    fun updateOzgecmis(){



        if (okunanKullaniciBilgileri.ozgecmis.equals("")){
            mView.tvOzgecmis.text = "Özgeçmişini Gir"
            mView.tvOzgecmis.setTextColor(ContextCompat.getColor(activity!!,R.color.mavi))
            mView.tvOzgecmis.background = null
        }else{
            mView.tvOzgecmis.text = okunanKullaniciBilgileri.ozgecmis
            mView.tvOzgecmis.setTextColor(ContextCompat.getColor(activity!!,R.color.siyah))
            mView.tvOzgecmis.setBackgroundResource(R.drawable.button_inactive)

        }
    }

    //arabanın varyok durumunu gösteren karşılaştırmalar burada yer almaktadır
    fun updateCar(){



        if (okunanKullaniciBilgileri.car!!.marka.equals("")){
            mView.profileArabaModel.text = "Araba Ekle"
            mView.profileArabaModel.setTextColor(ContextCompat.getColor(activity!!,R.color.mavi))
            mView.profileArabaRenk.text =""
            mView.profileArabaFoto.setImageResource(R.drawable.bottom_home)
        }else{
            UniversalImageLoader.setImage(okunanKullaniciBilgileri.car!!.pic!!,mView.profileArabaFoto!!,null,"")
            mView.profileArabaModel.text = okunanKullaniciBilgileri.car!!.marka+" / " + okunanKullaniciBilgileri.car!!.model
            mView.profileArabaRenk.text = okunanKullaniciBilgileri.car!!.renk
            mView.profileArabaModel.setTextColor(ContextCompat.getColor(activity!!,R.color.siyah))
        }
    }

    //tercih durumlarını burada set editoruz
    fun updateTercihler(){
        if (okunanKullaniciBilgileri.tercihler!!.talk!! == 0){
            mView.profileTercihlerTalk.setImageResource(R.drawable.question)
        }else if (okunanKullaniciBilgileri.tercihler!!.talk!! == 1){
            mView.profileTercihlerTalk.setImageResource(R.drawable.talkative)
        }else{
            mView.profileTercihlerTalk.setImageResource(R.drawable.no_talkative)
        }

        if (okunanKullaniciBilgileri.tercihler!!.smoke!! == 0){
            mView.profileTercihlerSmoke.setImageResource(R.drawable.question)
        }else if (okunanKullaniciBilgileri.tercihler!!.smoke!! == 1){
            mView.profileTercihlerSmoke.setImageResource(R.drawable.smoke)
        }else{
            mView.profileTercihlerSmoke.setImageResource(R.drawable.no_smoke)
        }

        if (okunanKullaniciBilgileri.tercihler!!.music!! == 0){
            mView.profileTercihlerMusic.setImageResource(R.drawable.question)
        }else if (okunanKullaniciBilgileri.tercihler!!.music!! == 1){
            mView.profileTercihlerMusic.setImageResource(R.drawable.music)
        }else{
            mView.profileTercihlerMusic.setImageResource(R.drawable.no_music)
        }

        if (okunanKullaniciBilgileri.tercihler!!.pet!! == 0){
            mView.profileTercihlerPet.setImageResource(R.drawable.question)
        }else if (okunanKullaniciBilgileri.tercihler!!.pet!! == 1){
            mView.profileTercihlerPet.setImageResource(R.drawable.pet)
        }else{
            mView.profileTercihlerPet.setImageResource(R.drawable.no_pet)
        }



    }

    //veri tabanından kullanıcı bilgileri çekilir
    private fun kullaniciBilgileriniGetir() {

        mRef.child("users").child(mUser.uid).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    okunanKullaniciBilgileri = p0.getValue(Users::class.java)!!
                    updateOzgecmis()
                    updateCar()
                    updateTercihler()

                }

            }

        })

    }



    /*@Subscribe(sticky = true)
    internal fun onProfileEvent(kullaniciBilgileri: EventbusDataEvents.KullaniciBilgileriniGonder){
        ozgecmis = kullaniciBilgileri.kullanici!!.ozgecmis!!


    }*/

   /* override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }*/

}

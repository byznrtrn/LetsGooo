package com.example.letsgoo.Login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.letsgoo.Model.Car
import com.example.letsgoo.Model.Rozetler
import com.example.letsgoo.Model.Tercihler
import com.example.letsgoo.Model.Users

import com.example.letsgoo.R
import com.example.letsgoo.utils.EventbusDataEvents
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_kayit.*
import kotlinx.android.synthetic.main.fragment_kayit.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Email ile giriş yapılacaksa gerekli diğer bilgiler alınacak bu class'ta
 */
class KayitFragment : Fragment() {

    var gelenSifre = ""
    var gelenEmail = ""

    lateinit var mAuth: FirebaseAuth
    lateinit var mRef: DatabaseReference
    var isEdu = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.fragment_kayit, container, false)

        mAuth = FirebaseAuth.getInstance()
        mRef = FirebaseDatabase.getInstance().reference


        //edittextler için watcher atandı
        view.etAdSoyad.addTextChangedListener(watcher)
        view.etKullaniciAdi.addTextChangedListener(watcher)

        view.btnGiris.setOnClickListener {



            //girilen bilgilerin uygunluğu kontrol ediliyor
            mRef.child("users").orderByChild("username").equalTo(etKullaniciAdi.text.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        println(p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            Toast.makeText(activity!!,"Kullanıcı adı kullanımda",Toast.LENGTH_SHORT).show()
                        }else{
                            var username = view.etKullaniciAdi.text.toString()
                            var adSoyad = view.etAdSoyad.text.toString()

                            mAuth.createUserWithEmailAndPassword(gelenEmail,gelenSifre)
                                .addOnCompleteListener { p0 ->
                                    if (p0.isSuccessful){
                                        //eğer tüm şartlar sağlanırsa kullanıcı bilgileri ilklendirilip veritabanına user kaydediliyor
                                        var userID = mAuth.currentUser!!.uid
                                        val rozetler = Rozetler(isEdu,0,0,0,0)
                                        val car = Car("","","","","",0,"")
                                        val tercihler = Tercihler(0,0,0,0)
                                        val kaydedilecekKullanıcıBilgisi = Users(gelenEmail,username,adSoyad,"Bilinmiyor","",userID,"","","",0,rozetler,car,tercihler,1)

                                        mRef.child("users").child(userID).setValue(kaydedilecekKullanıcıBilgisi)
                                            .addOnCompleteListener { p0 ->
                                                if (p0.isSuccessful){
                                                    println("başarılı")
                                                    Toast.makeText(activity!!,"Kullanıcı verileri kaydedildi",Toast.LENGTH_SHORT).show()
                                                }else{
                                                    mAuth.currentUser!!.delete()
                                                        .addOnCompleteListener { p0 ->
                                                            if (p0.isSuccessful){
                                                                Toast.makeText(activity!!,"Kullanıcı kaydedilemedi lütfen tekrar deneyiniz",Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                }
                                            }
                                        Toast.makeText(activity!!,"Oturum Açıldı",Toast.LENGTH_SHORT).show()
                                    }else{
                                        Toast.makeText(activity!!,"Olmadı",Toast.LENGTH_SHORT).show()
                                        println(p0)
                                    }
                                }
                        }
                    }
                })
        }
        return view
    }



    var watcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s!!.length > 3){

                if (etAdSoyad.text.toString().length > 3  && etKullaniciAdi.text.toString().length > 3){
                    btnGiris.isEnabled =true
                    btnGiris.setTextColor(ContextCompat.getColor(activity!!,R.color.beyaz))
                    btnGiris.setBackgroundResource(R.drawable.button_active)
                }

            }else{
                btnGiris.isEnabled =false
                btnGiris.setTextColor(ContextCompat.getColor(activity!!,R.color.sonukmavi))
                btnGiris.setBackgroundResource(R.drawable.button_inactive)
                //Toast.makeText(activity!!,"Her iki alan  için en az 6 karakter giriniz", Toast.LENGTH_SHORT).show()

            }
        }


    }

    //register activityden gelen bilgilerin alındığı fonksiyon
    @Subscribe(sticky = true)
    internal fun onKayitEvent(kayitBilgileri: EventbusDataEvents.KayitBilgileriniGonder){


            gelenSifre = kayitBilgileri.sifre!!
            gelenEmail = kayitBilgileri.email!!


            if (gelenEmail.split("@")[1].contains(".edu.")){
                isEdu = 1
            }


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }


}

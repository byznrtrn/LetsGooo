package com.eskisehir.letsgo.Profil

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eskisehir.letsgo.Login.FirstActivity

import com.eskisehir.letsgo.R
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_accunt.view.*

/**
 * Hesap bilgilerindeki bölümler için activitylere yönlendirme yapılacak
 */
class AccountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //AttachToRoot false olduğunda, ilk parametreden layout dosyası şişirilir ve
        // Görünüm olarak döndürülür. Döndürülen Görünümün kökü, düzen dosyasında belirtilen kök olacaktır.
        val view = inflater.inflate(R.layout.fragment_accunt, container, false)


        view.accVerdigimPuanlar.setOnClickListener {
            //verdiğim puanlara tıklandığında
           //  SetOnClickListener bloğunun içindeki kod yürütülür.
            val intent = Intent(activity!!,VerilenPuanlar::class.java)
            startActivity(intent)
        }

        view.accSifreDegis.setOnClickListener {
            //şifredeğiştire tıklandığında
            //  SetOnClickListener bloğunun içindeki kod yürütülür.
            val intent = Intent(activity!!,SifreDegistir::class.java)
            startActivity(intent)
        }

        view.accBakiye.setOnClickListener {
            //bakiyeye tıklandığında
            //  SetOnClickListener bloğunun içindeki kod yürütülür.
            val intent = Intent(activity!!,BirikenBakiye::class.java)
            startActivity(intent)
        }

        view.accRezervasyonGecmisi.setOnClickListener {
            //rezervasyon geçmişine  tıklandığında
            //  SetOnClickListener bloğunun içindeki kod yürütülür.
            val intent = Intent(activity!!,RezervasyonGecmisi::class.java)
            startActivity(intent)
        }

        view.accTransferGecmisi.setOnClickListener {
            //transfer geçmişine tıklandığında
            //  SetOnClickListener bloğunun içindeki kod yürütülür.
            val intent = Intent(activity!!,TransferGecmisi::class.java)
            startActivity(intent)
        }


        view.accCikisYap.setOnClickListener {
            //cıkışyapa tıklanınca çıkış işlemi yapılır
           // FirebaseAuth.getInstance().signOut()-->Firebase yetkilendirme, çıkış yöntemidir.
            FirebaseAuth.getInstance().signOut()

            AuthUI.getInstance().signOut(activity!!)
                .addOnCompleteListener {
                    startActivity(Intent(activity!!, FirstActivity::class.java))
                    activity!!.finish()
                }
        }









        return view

    }

}

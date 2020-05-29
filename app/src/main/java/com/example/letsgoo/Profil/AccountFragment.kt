package com.example.letsgoo.Profil

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
        val view = inflater.inflate(R.layout.fragment_accunt, container, false)


        view.accVerdigimPuanlar.setOnClickListener {
            val intent = Intent(activity!!,VerilenPuanlar::class.java)
            startActivity(intent)
        }

        view.accSifreDegis.setOnClickListener {
            val intent = Intent(activity!!,SifreDegistir::class.java)
            startActivity(intent)
        }

        view.accBakiye.setOnClickListener {
            val intent = Intent(activity!!,BirikenBakiye::class.java)
            startActivity(intent)
        }

        view.accRezervasyonGecmisi.setOnClickListener {
            val intent = Intent(activity!!,RezervasyonGecmisi::class.java)
            startActivity(intent)
        }

        view.accTransferGecmisi.setOnClickListener {
            val intent = Intent(activity!!,TransferGecmisi::class.java)
            startActivity(intent)
        }


        view.accCikisYap.setOnClickListener {
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

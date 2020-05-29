package com.example.letsgoo.Profil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

import com.eskisehir.letsgo.R

/**
 * bir işlemin devam ettiğini göstermek için burası kullanılır
 */
class YukleniyorFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_yukleniyor, container, false)
    }

}

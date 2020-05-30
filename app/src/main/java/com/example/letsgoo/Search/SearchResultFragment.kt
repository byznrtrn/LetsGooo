package com.example.letsgoo.Search

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsgoo.Model.Ilan


import com.example.letsgoo.R
import com.example.letsgoo.utils.EventbusDataEvents
import com.example.letsgoo.utils.HomeFragmentAdapter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.fragment_search_result.*
import kotlinx.android.synthetic.main.fragment_search_result.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * /aranan yolculuk sonuçları burada görüntülenecek
 */




class SearchResultFragment : Fragment() {

    lateinit var rootView:View
    var nerden = ""
    var nereye = ""
    var date = ""
    var lg = 0.0
    var lt = 0.0
    lateinit var mRef : DatabaseReference
    private val ilanlar:ArrayList<Ilan> = ArrayList()
    var adapter : HomeFragmentAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_search_result, container, false)

        rootView.mapteGoster.setOnClickListener {

        adapter = HomeFragmentAdapter(context!!,ilanlar)
            if (ilanlar.size>0){

                val intent = Intent(activity,SearchResultMapActivity::class.java)
                EventBus.getDefault().postSticky(EventbusDataEvents.aramaSonucBilgileriniGonder(ilanlar))
                startActivity(intent)

            }else{
                Toast.makeText(activity, "Lütfen tüm ilanlar getirilene kadar bekleyiniz", Toast.LENGTH_LONG).show()
            }


        }

        mRef = FirebaseDatabase.getInstance().reference

        return rootView
    }



    //event bus ile gelenverilere göre arama yapılıyor
    private fun ilanBul() {
        //veri tabanıına kaydediliği şekilde araama texti hazırlanır
       val rota = nerden+"&"+nereye+"&"+date
        mRef.child("ilanlar").orderByChild("rotaDate").equalTo(rota)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            for (ilan in p0.children){
                                ilanlar.clear()
                                for (ilan in p0.children){
                                    val gelenIlan = ilan.getValue(Ilan::class.java)
                                    val unix =System.currentTimeMillis() / 1000L;
                                    if (gelenIlan!!.userId!!.split("&")[1].equals("true")){
                                        ilanlar.add(gelenIlan!!)
                                    }

                                }
                                //sonuç varsa gösterilir yoksa bulunamadı yazısı gösterilir
                                if (ilanlar.size>0){
                                    rvResult!!.layoutManager = LinearLayoutManager(context)
                                    adapter = HomeFragmentAdapter(context!!,ilanlar)
                                    rvResult.adapter = adapter
                                }else{
                                    rvResult!!.layoutManager = LinearLayoutManager(context)
                                    adapter = HomeFragmentAdapter(context!!,ilanlar)
                                    rvResult.adapter = adapter
                                    rootView.tvSearchResult.visibility = View.VISIBLE
                                    rootView.mapteGoster.visibility = View.GONE
                                }

                            }
                        }else{
                            rootView.tvSearchResult.visibility = View.VISIBLE
                        }
                    }
                })
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    internal fun onMySearchResult(aramaBilgileri: EventbusDataEvents.aramaBilgileriniGonder){

        nerden = aramaBilgileri.nerden!!
        nereye = aramaBilgileri.nereye!!
        date = aramaBilgileri.date
        lt = aramaBilgileri.lt!!
        lg = aramaBilgileri.lg!!

        ilanBul()


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

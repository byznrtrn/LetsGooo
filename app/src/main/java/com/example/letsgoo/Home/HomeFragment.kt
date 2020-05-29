package com.example.letsgoo.Home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.eskisehir.letsgo.Login.FirstActivity
import com.eskisehir.letsgo.Model.Ilan

import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.EventbusDataEvents
import com.eskisehir.letsgo.utils.Globals
import com.eskisehir.letsgo.utils.HomeFragmentAdapter
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * A simple [Fragment] subclass.
 */

//aktif süreçte alakalı olunan ilanlar burada gösterilecek

class HomeFragment : Fragment() {

    lateinit var mRef:DatabaseReference
    private val ilanlar:ArrayList<Ilan> = ArrayList()
    private val basvurular:ArrayList<Ilan> = ArrayList()
    private val basvuruId:ArrayList<String> = ArrayList()
    private val all:ArrayList<Ilan> = ArrayList()
    var adapter:HomeFragmentAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        mRef = FirebaseDatabase.getInstance().reference

        //ilanlar ve başvurular çekilip recyclerView içerisinde gösterilecek
        getIlan()
        getBasvuru()


       val gl = Globals.letsGo
        gl.homeFragment = this

        return view
    }

    //herhangi bi değişiklik olduğunda bu değişikliği yansıtmak için yeniden yükleme işlemi yapılır
    fun reload(){
        all.clear()
        getIlan()
        getBasvuru()
    }

    //başvurular çekilir ve arraliste atanır
    private fun getBasvuru() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        mRef.child("basvurular").child(FirebaseAuth.getInstance().currentUser!!.uid).orderByChild("userId").equalTo(userId+"&true")
                .addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            basvurular.clear()
                            for (ilan in p0.children){
                                val ilanId = ilan.child("ilanId").getValue(String::class.java)
                                basvuruId.add(ilanId!!)
                            }
                            val bSize = basvuruId.size
                            var eklenenSize = 0

                            for (id in basvuruId){
                                mRef.child("ilanlar").child(id).addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onCancelled(p0: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        if (p0.exists()){
                                            val ilan = p0.getValue(Ilan::class.java)
                                            basvurular.add(ilan!!)
                                            eklenenSize++
                                            //ilan ya da başvuru hangisi önce çekilecke bilmiyoruz o yüzden çekilen veriler kontrollü bi şekilde eklenmesi için fonksiyon kullandık
                                            setAdapter(bSize,eklenenSize)
                                        }
                                    }

                                })
                            }

                        }else{
                            if (!all.isEmpty()){
                                all.clear()
                                all.addAll(ilanlar)
                                adapter!!.notifyDataSetChanged()
                            }
                        }
                    }

                })
    }

    fun setAdapter(bSize: Int, eklenenSize :Int){
        //eklenen boyuta eşit olmadan işlem yapılmayacak
        //eşit olunca eğer boşsa direk ekleyecek
        if (bSize == eklenenSize){
            if (all.isEmpty()){
                all.addAll(basvurular)
                rvHome!!.layoutManager =LinearLayoutManager(context)
                adapter =HomeFragmentAdapter(context!!,all)
                rvHome.adapter = adapter
            }else{
                //bunun bir güncelleme mi yoksa yenileme mi olduğunu bilmediğimizde herşeyi silip güncel hali ekleme yaptık
                all.clear()
                all.addAll(ilanlar)
                all.addAll(basvurular)
                adapter!!.notifyDataSetChanged()
            }
        }
    }

    //ilanlarda başvurular gibi çekilip arrayliste eklendi
    private fun getIlan() {

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        mRef.child("ilanlar").orderByChild("userId").equalTo(userId+"&true")
                .addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            ilanlar.clear()
                            for (ilan in p0.children){
                                val gelenIlan = ilan.getValue(Ilan::class.java)
                                ilanlar.add(gelenIlan!!)
                            }

                            if (all.isEmpty()){
                                all.addAll(ilanlar)
                                rvHome!!.layoutManager =LinearLayoutManager(context)
                                adapter =HomeFragmentAdapter(context!!,all)
                                rvHome.adapter = adapter
                            }else{
                                all.clear()
                                all.addAll(ilanlar)
                                all.addAll(basvurular)
                                adapter!!.notifyDataSetChanged()
                            }


                        }else{
                            println("yok öyle bişi")
                        }
                    }

                })

    }


    @Subscribe(sticky = true)
    internal fun onMode(mode: EventbusDataEvents.modeGonder){



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

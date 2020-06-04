package com.example.letsgoo.Message

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.letsgoo.Login.FirstActivity
import com.example.letsgoo.Model.Konusmalar

import com.example.letsgoo.R
import com.example.letsgoo.utils.KonusmalarRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_message.view.*

/**
 * Mevcut mesajlar listelenecek
 */
class MessageFragment : Fragment() {

    lateinit var mAuth: FirebaseAuth
    lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    var tumKonusmalar: ArrayList<Konusmalar> = ArrayList<Konusmalar>()
    lateinit var myRecyclerView: androidx.recyclerview.widget.RecyclerView
    lateinit var myLinearLayoutManager: androidx.recyclerview.widget.LinearLayoutManager
    lateinit var myAdapter: KonusmalarRecyclerAdapter
    lateinit var myFragmentView: View

    lateinit var mRef: DatabaseReference
    var listenerAtandiMi=false

    companion object {
        var fragmentAcikMi=false
    }
//mesajlarla ilgili içerik oluşturuluyor
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        myFragmentView = inflater.inflate(R.layout.fragment_message, container, false)

        setupAuthListener()
        mAuth = FirebaseAuth.getInstance()





        myFragmentView.progressBar6.visibility=View.VISIBLE
        myFragmentView.recyclerKonusmalar.visibility=View.INVISIBLE
        setupKonusmalarRecylerView()


        return myFragmentView
    }


    //firebasede olan tüm mesajlar getirilecek
    private fun tumKonusmalariGetir() {



        //current usera ait konuşmalar çekiliyor
        mRef = FirebaseDatabase.getInstance().reference.child("konusmalar").child(mAuth.currentUser!!.uid)
        if(listenerAtandiMi==false){
            listenerAtandiMi=true
            mRef.orderByChild("time").addChildEventListener(myListener)

            object : CountDownTimer(1000,1000){
                override fun onFinish() {
                    myFragmentView.progressBar6.visibility=View.GONE
                    myFragmentView.recyclerKonusmalar.visibility=View.VISIBLE
                }

                override fun onTick(p0: Long) {

                }

            }.start()

        }


    }


    //konuşmada olan değişikliği algılayıp canlı olarak yeni mesaj çekilecek. Mesaj dalının altında bi değişiklik olursa burası çalışır
    private var myListener = object : ChildEventListener {
        override fun onCancelled(p0: DatabaseError) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            //kontrol -1 ise yeni bir konusma listeye eklenecek, -1den farklı ise var olan konusmanın arraylistte
            //position degeridir.
            var kontrol =konusmaPositionBul(p0!!.key.toString())
            if(kontrol != -1){

                var guncellenecekKonusma = p0!!.getValue(Konusmalar::class.java)
                guncellenecekKonusma!!.user_id=p0!!.key

                // myRecyclerView.recycledViewPool.clear()
                tumKonusmalar.removeAt(kontrol)
                myAdapter.notifyItemRemoved(kontrol)
                tumKonusmalar.add(0,guncellenecekKonusma)
                myAdapter.notifyItemInserted(0)


            }



        }

        //yeni mesaj eklendiğinde burası çalışır

        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            var eklenecekKonusma = p0!!.getValue(Konusmalar::class.java)
            eklenecekKonusma!!.user_id=p0!!.key
            tumKonusmalar.add(0,eklenecekKonusma!!)

            myAdapter.notifyItemInserted(0)
        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

    }


    //listelenen konuşmanın posizyonunu getirir
    private fun konusmaPositionBul(userID : String) : Int{

        for(i in 0..tumKonusmalar.size-1){
            var gecici = tumKonusmalar.get(i)

            if(gecici.user_id.equals(userID)){
                return i
            }
        }

        return -1


    }


    //çekilen mesajlar recyclerview e koyulacak
    private fun setupKonusmalarRecylerView() {

        myRecyclerView = myFragmentView.recyclerKonusmalar
        myLinearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        myAdapter = KonusmalarRecyclerAdapter(tumKonusmalar, this!!.activity!!)

        myRecyclerView.layoutManager = myLinearLayoutManager
        myRecyclerView.adapter = myAdapter


        tumKonusmalariGetir()


    }

    private fun setupAuthListener() {
        mAuthListener = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                //Bu method(onAuthStateChanged), kimlik doğrulama durumundaki değişikliklerde UI iş parçacığında çağrılır:
                //kimlik doğrulama durumundaki değişiklikler
                //*Dinleyici kaydedildikten hemen sonra
                //**Bir kullanıcı oturum açtığında
                //***Geçerli kullanıcının oturumu kapatıldığında***
                //****Geçerli kullanıcı değiştiğinde
                var user = FirebaseAuth.getInstance().currentUser

                if (user == null) {
                    //Log.e("HATA", "Kullanıcı oturum açmamış, HomeActivitydesn")
                    var intent = Intent(activity, FirstActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    activity!!.finish()
                } else {


                }
            }

        }
    }

    //aşağıdaki kısımlar . Uygulamanın arkaplana alınması fragmente yeniden gelinmesi vs durumlarda en güncel hali görüntülemek için
    override fun onPause() {
        fragmentAcikMi=false
        super.onPause()

        tumKonusmalar.clear()
        if(listenerAtandiMi==true){
            listenerAtandiMi=false
            mRef.removeEventListener(myListener)
        }

    }

    override fun onStart() {
        fragmentAcikMi=true
        super.onStart()
    }
    override fun onStop() {
        fragmentAcikMi=false
        super.onStop()
    }
    override fun onResume() {
        super.onResume()
        fragmentAcikMi=true
        tumKonusmalar.clear()
        if(listenerAtandiMi==false){
            listenerAtandiMi=true
            myAdapter.notifyDataSetChanged()
            mRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    myFragmentView.progressBar6.visibility=View.VISIBLE
                    myFragmentView.recyclerKonusmalar.visibility=View.GONE
                    mRef.orderByChild("time").addChildEventListener(myListener)
                    object : CountDownTimer(1000,1000){
                        override fun onFinish() {
                            myFragmentView.progressBar6.visibility=View.GONE
                            myFragmentView.recyclerKonusmalar.visibility=View.VISIBLE
                        }

                        override fun onTick(p0: Long) {

                        }

                    }.start()
                }

            })

        }
    }
}

package com.eskisehir.letsgo.Message

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import com.dinuscxj.refresh.RecyclerRefreshLayout
import com.eskisehir.letsgo.Login.FirstActivity
import com.eskisehir.letsgo.Model.Mesaj
import com.eskisehir.letsgo.Model.Users
import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.MesajRecyclerViewAdapter
import com.eskisehir.letsgo.utils.UniversalImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chat.*


//mesajlaşma ekranı
class ChatActivity : AppCompatActivity() {

    lateinit var sohbetEdilecekUserId:String
    lateinit var mesajGonderenUserId:String
    lateinit var mAuth: FirebaseAuth
    lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    lateinit var mRef:DatabaseReference
    lateinit var mYaziyorRef:DatabaseReference
    lateinit var mDinleYaziyorRef:DatabaseReference
    var tumMesajlar:ArrayList<Mesaj> = ArrayList<Mesaj>()
    lateinit var myRecyclerViewAdapter: MesajRecyclerViewAdapter
    lateinit var myRecyclerView: androidx.recyclerview.widget.RecyclerView
    var sohbetEdilecekUser:Users? = null

    var ekrandaSonGorulmeVarMi=false

    companion object {
        var activityAcikMi=false
    }



    //sayfalamaiçin
    val SAYFA_BASI_GONDERI_SAYISI = 10
    var sayfaNumarasi=1

    var mesajPos=0
    var dahaFazlaMesajPos=0
    var ilkGetirilenMesajID=""
    var zatenListedeOlanMesajID=""

    lateinit var childEventListener:ChildEventListener
    lateinit var childListenerDahaFazla:ChildEventListener




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setupAuthListener()
        //firebase databese referansı mRef tanımlandı
        mAuth = FirebaseAuth.getInstance()
        mRef=FirebaseDatabase.getInstance().reference

        progressBar5.visibility= View.VISIBLE
        tvSohbetEdilecekUserName.visibility= View.INVISIBLE
        rvSohbet.visibility= View.INVISIBLE
        //kimle sohbet edeceksek onu id si belirlenir
        if(intent.extras.get("userId") != null){
            var id=intent.extras.get("userId").toString()
            sohbetEdilecekUserId=id
        }

        mesajGonderenUserId=mAuth.currentUser!!.uid.toString()
        //mesaj atan ve mesaj alan durumuna göre yazıyor ibaresi için referans belirlendi
        mYaziyorRef=FirebaseDatabase.getInstance().reference.child("konusmalar").child(mesajGonderenUserId).child(sohbetEdilecekUserId)
        mDinleYaziyorRef=FirebaseDatabase.getInstance().reference.child("konusmalar").child(sohbetEdilecekUserId).child(mesajGonderenUserId)

        sohbetEdenlerinBilgileriniGetir(sohbetEdilecekUserId, mesajGonderenUserId)

       // Geri aramadaki Yenileme işlemi için OnRefreshListener öğesini RecyclerRefreshListener olarak ayarladık .
        refreshLayout.setOnRefreshListener(object : RecyclerRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
//refresh metodu çağırılır


                mRef.child("mesajlar").child(mesajGonderenUserId).child(sohbetEdilecekUserId).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                  //mesajları getir

                        if(p0!!.childrenCount.toInt() != tumMesajlar.size){
                            dahaFazlaMesajPos=0


                            dahaFazlaMesajGetir()
                        }else{
//refleshLayout -->pull-to-refresh library
//mesajlar geldikten sonra refresh duracak
                            //sürekli refresh etmemesi için false değerini ayarladık
                            refreshLayout.setRefreshing(false)
                            refreshLayout.setEnabled(false)
                        }
                    }


                })





            }

        })





        //mesaj gönderileceği zaman mesajın barındırdığı bilgileri bir map yapısından veritabanına eklenir
        tvMesajGonderButton.setOnClickListener {

            var mesajText=etMesaj.text.toString().trim()

            if(!TextUtils.isEmpty(mesajText.toString())){
                //mesajlar branchına eklemeler yapıldı
                var mesajAtan=HashMap<String,Any>()
                mesajAtan.put("mesaj",mesajText)
                mesajAtan.put("goruldu",true)
                mesajAtan.put("time",ServerValue.TIMESTAMP)
                mesajAtan.put("type","text")
                mesajAtan.put("user_id",mesajGonderenUserId)

                var yeniMesajKey=mRef.child("mesajlar").child(mesajGonderenUserId).child(sohbetEdilecekUserId).push().key
                if (yeniMesajKey != null) {
                    mRef.child("mesajlar").child(mesajGonderenUserId).child(sohbetEdilecekUserId).child(yeniMesajKey).setValue(mesajAtan)
                }

                //mesajı bide alan kişi için kaydetmemiz gerekir
                var mesajAlan=HashMap<String,Any>()
                mesajAlan.put("mesaj",mesajText)
                mesajAlan.put("goruldu",false)
                mesajAlan.put("time",ServerValue.TIMESTAMP)
                mesajAlan.put("type","text")
                mesajAlan.put("user_id",mesajGonderenUserId)
                mRef.child("mesajlar").child(sohbetEdilecekUserId).child(mesajGonderenUserId).child(yeniMesajKey!!).setValue(mesajAlan)


                //mesaja ait ek bilgilerde veritabanına map yapısında eklenir
                var konusmaMesajAtan=HashMap<String,Any>()
                konusmaMesajAtan.put("time",ServerValue.TIMESTAMP)
                konusmaMesajAtan.put("goruldu",true)
                konusmaMesajAtan.put("son_mesaj",mesajText)
                konusmaMesajAtan.put("typing",false)

                mRef.child("konusmalar").child(mesajGonderenUserId).child(sohbetEdilecekUserId).setValue(konusmaMesajAtan)

                var konusmaMesajAlan=HashMap<String,Any>()
                konusmaMesajAlan.put("time",ServerValue.TIMESTAMP)
                konusmaMesajAlan.put("goruldu",false)
                konusmaMesajAlan.put("son_mesaj",mesajText)
                //konusmaMesajAlan.put("typing",false)


                mRef.child("konusmalar").child(sohbetEdilecekUserId).child(mesajGonderenUserId).setValue(konusmaMesajAlan)


                etMesaj.setText("")
            }





        }
//call back uygulanması kullanımında object gerekli
        etMesaj.addTextChangedListener(object : TextWatcher {

            var typing=false

            override fun afterTextChanged(p0: Editable?) {

                if(!TextUtils.isEmpty(p0.toString()) && p0!!.toString().trim().length == 1){
                    typing=true
                    //Log.e("KONTROL","KULLANICI YAZMAYA BASLAMIS")
                    mYaziyorRef.child("typing").setValue(true)
                }else if(typing && p0!!.toString().trim().length == 0){
                    typing=false
                    //Log.e("KONTROL","KULLANICI YAZMAYI BIRAKTI")
                    mYaziyorRef.child("typing").setValue(false)
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })

        imgBack.setOnClickListener {
            onBackPressed()
        }


    }


    //yukarı doğru scroll yapıldığında varsa fazla mesajlar eklenir
    private fun dahaFazlaMesajGetir(){


        childListenerDahaFazla= mRef.child("mesajlar").child(mesajGonderenUserId).child(sohbetEdilecekUserId)
                .orderByKey().endAt(ilkGetirilenMesajID).limitToLast(SAYFA_BASI_GONDERI_SAYISI)
                .addChildEventListener(object : ChildEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {

                    }

                    override fun onChildChanged(p0: DataSnapshot, p1: String?) {

                    }

                    override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                        //Log.e("KONTROL","p0 kaç tane veri geldi "+p0!!.childrenCount+" pos:"+dahaFazlaMesajPos)
                        var okunanMesaj=p0!!.getValue(Mesaj::class.java)

                        if(!zatenListedeOlanMesajID.equals(p0!!.key)){
                            tumMesajlar.add(dahaFazlaMesajPos++,okunanMesaj!!)
                            myRecyclerViewAdapter.notifyItemInserted(dahaFazlaMesajPos-1)
                        }else {

                            zatenListedeOlanMesajID=ilkGetirilenMesajID

                        }

                        if(dahaFazlaMesajPos==1){

                            ilkGetirilenMesajID= p0!!.key!!

                        }


                        //Log.e("KONTROL","ZATEN LİSTEDEKI ID:"+zatenListedeOlanMesajID+" ILK GETIRILIEN ID:"+ilkGetirilenMesajID+" mesaj id:"+p0!!.key)





                        myRecyclerView.scrollToPosition(0)

                        refreshLayout.setRefreshing(false)

                    }

                    override fun onChildRemoved(p0: DataSnapshot) {

                    }

                })





    }


    //ilk açılıştaki mesajlar çekilir
    //yeni mesaj geldiğinde canlı olarak yine burası çalışır
    private fun mesajlariGetir() {

        childEventListener= mRef.child("mesajlar").child(mesajGonderenUserId).child(sohbetEdilecekUserId).limitToLast(SAYFA_BASI_GONDERI_SAYISI).addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                var okunanMesaj=p0!!.getValue(Mesaj::class.java)
                tumMesajlar.add(okunanMesaj!!)

                if(mesajPos==0){

                    ilkGetirilenMesajID= p0!!.key!!
                    zatenListedeOlanMesajID= p0!!.key!!

                }
                mesajPos++

                mesajGorulduBilgisiniGuncelle(p0!!.key)

                sonGorulmeBilgisiniGuncelle(p0!!.key)



                myRecyclerViewAdapter.notifyItemInserted(tumMesajlar.size-1)
                myRecyclerView.scrollToPosition(tumMesajlar.size-1)

                //Log.e("KONTROL","İLK OKUNAN MESAJ ID :"+ilkGetirilenMesajID)


            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })

        object : CountDownTimer(1000,1000){
            override fun onFinish() {
                progressBar5.visibility= View.GONE
                tvSohbetEdilecekUserName.visibility= View.VISIBLE
                rvSohbet.visibility= View.VISIBLE
            }

            override fun onTick(p0: Long) {

            }

        }.start()

    }


    //firebasedeki ilgili yerler kontrol edilerek görüldü ve yazıyor bilgileri canlı olarak değiştirilir
    private fun sonGorulmeBilgisiniGuncelle(mesajID: String?) {

        if (mesajID != null) {
            FirebaseDatabase.getInstance().getReference().child("mesajlar").child(sohbetEdilecekUserId).child(mesajGonderenUserId)
                    .child(mesajID).addValueEventListener(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            if(p0!!.child("goruldu").getValue() == true && p0!!.child("user_id").getValue().toString().equals(mesajGonderenUserId)){
                                ekrandaSonGorulmeVarMi=true
                                sonGorulmeContainer.visibility= View.VISIBLE
                            }else {
                                ekrandaSonGorulmeVarMi=false
                                sonGorulmeContainer.visibility= View.GONE
                            }

                        }

                    })
        }



    }

    private fun mesajGorulduBilgisiniGuncelle(mesajID: String?) {

        if (mesajID != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("mesajlar").child(mesajGonderenUserId).child(sohbetEdilecekUserId).child(mesajID)
                    .child("goruldu").setValue(true)
                    .addOnCompleteListener {
                        FirebaseDatabase.getInstance().getReference().child("konusmalar")
                                .child(mesajGonderenUserId).child(sohbetEdilecekUserId).child("goruldu").setValue(true)
                    }//write to database
        }           //you can use setValue() to save data to a specified reference


    }


    private var yaziyorEventListener=object : ValueEventListener{
        override fun onCancelled(p0: DatabaseError) {

        }

        override fun onDataChange(p0: DataSnapshot) {

            if(p0!!.getValue()!=null){
                if(p0!!.getValue() == true){
                    //Log.e("kontrol","value event listener tetiklendi"+p0!!.getValue()!!.toString())

                    if(ekrandaSonGorulmeVarMi){
                        sonGorulmeContainer.visibility= View.GONE
                    }

                    yaziyorContainer.visibility= View.VISIBLE
                    yaziyorContainer.startAnimation(AnimationUtils.loadAnimation(this@ChatActivity,android.R.anim.fade_in))


                }else if(p0!!.getValue() == false){
                    //Log.e("kontrol","değer false olmus")

                    if(ekrandaSonGorulmeVarMi){
                        sonGorulmeContainer.visibility= View.VISIBLE
                    }

                    yaziyorContainer.visibility= View.GONE
                    yaziyorContainer.startAnimation(AnimationUtils.loadAnimation(this@ChatActivity,android.R.anim.fade_out))
                }
            }



        }

    }

    private fun setupMesajlarRecyclerView() {
        //androidx.recyclerview.widget.LinearLayoutManager->layoutmanagerı ayarla
        var myLinearLayoutManager= androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        myLinearLayoutManager.stackFromEnd=true
        myRecyclerView=rvSohbet

        myRecyclerViewAdapter=MesajRecyclerViewAdapter(tumMesajlar, this, sohbetEdilecekUser!!)
        myRecyclerView.layoutManager=myLinearLayoutManager
        myRecyclerView.adapter=myRecyclerViewAdapter

        mesajlariGetir()

    }

    private fun sohbetEdenlerinBilgileriniGetir(sohbetEdilecekUserId: String?, oturumAcanUserID:String?) {

        if (sohbetEdilecekUserId != null) {
            //AddListenerForSingleValueEvent () öğesini çağırdığında,
            // referans alınan verileri Firebase sunucularından yüklemeye başlar
            mRef.child("users").child(sohbetEdilecekUserId).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0!!.getValue() != null){

                        sohbetEdilecekUser=p0!!.getValue(Users::class.java)
                        tvSohbetEdilecekUserName.setText(sohbetEdilecekUser!!.adi_soyadi!!.toString())
                        UniversalImageLoader.setImage(sohbetEdilecekUser!!.profile_picture!!.toString(),
                                circleImageViewYaziyor,null,"")
                        tvGorenKullaniciUserName.setText(sohbetEdilecekUser!!.adi_soyadi!!.toString())
                        setupMesajlarRecyclerView()
                    }
                }


            })
        }

        mRef.child("users").child(oturumAcanUserID!!).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0!!.getValue() != null){

                    var bulunanKullanici=p0!!.getValue(Users::class.java)
                    var imgUrl=bulunanKullanici!!.profile_picture.toString()
                    UniversalImageLoader.setImage(imgUrl,messageMyUserPic,null,"")
                }
            }


        })


    }


    private fun setupAuthListener() {
        mAuthListener = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                //geçerli kullanıcıyı almak için getCurrentUser metodu çağırılır
                var user = FirebaseAuth.getInstance().currentUser

                //  Oturum açmış kullanıcı yoksa getCurrentUser null değerini döndürür
                if (user == null) {
                    //Log.e("HATA", "Kullanıcı oturum açmamış, HomeActivitydesn")
                    var intent = Intent(this@ChatActivity, FirstActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                } else {


                }
            }

        }
    }

    override fun onStart() {
        super.onStart()
        if(activityAcikMi==false){
            activityAcikMi=true
        }else{
            activityAcikMi=true
        }

        //Log.e("HATA", "Chat activity on start")
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onStop() {
        super.onStop()
        if(activityAcikMi==true){
            activityAcikMi=false
        }else{
            activityAcikMi=false
        }
        mRef.child("mesajlar").child(mesajGonderenUserId).child(sohbetEdilecekUserId).removeEventListener(childEventListener)
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener)
        }
    }

    override fun onResume() {
        super.onResume()
        if(activityAcikMi==false){
            activityAcikMi=true
        }else{
            activityAcikMi=true
        }
        //Log.e("KONTROL",mYaziyorRef.toString())
        mDinleYaziyorRef.child("typing").addValueEventListener(yaziyorEventListener)
    }

    override fun onPause() {
        super.onPause()
        if(activityAcikMi==true){
            activityAcikMi=false
        }else{
            activityAcikMi=false
        }
        mYaziyorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0!!.hasChild("typing")) {
                    mYaziyorRef.child("typing").setValue(false)
                }
            }

        })
        mDinleYaziyorRef.child("typing").removeEventListener(yaziyorEventListener)
    }
}

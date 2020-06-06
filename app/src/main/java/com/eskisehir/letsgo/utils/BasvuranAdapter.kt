package com.eskisehir.letsgo.utils

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.eskisehir.letsgo.Home.BasvuranlarActivity
import com.eskisehir.letsgo.Message.ChatActivity
import com.eskisehir.letsgo.Model.Basvuranlar
import com.eskisehir.letsgo.Model.Users
import com.eskisehir.letsgo.Profil.AcikProfil
import com.eskisehir.letsgo.R
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView


//başvuranların listelendiği adapter

class BasvuranAdapter(context: Context, var basvuranlar: ArrayList<Basvuranlar>,kapasite:Int
): RecyclerView.Adapter<BasvuranAdapter.ViewHolder>() {


    lateinit var mRef : DatabaseReference
    var context = context
    var kapasaite = kapasite

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{

        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.basvuran_view,parent,false)
        mRef = FirebaseDatabase.getInstance().reference



        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {

        var user : Users? = null


        //user id ye göre başvuran bilgileir çekilir
        mRef.child("users").child(basvuranlar[position].userId).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    user = p0.getValue(Users::class.java)
                    UniversalImageLoader.setImage(user!!.profile_picture!!,holder.userPic,null,"")
                    holder.adSoyad.text = user!!.adi_soyadi
                }
            }

        })

        if (!(basvuranlar[position].onay == 0)){
            holder.check.isChecked = true
            kapasaite--
            (context as BasvuranlarActivity).guncelKapasite = kapasaite
        }else{
            holder.check.isChecked =false
        }


        //user açık profiline gider
        holder.parent.setOnClickListener {

            val intent = Intent(context,AcikProfil::class.java)
            intent.putExtra("userId",basvuranlar[position].userId)
            context.startActivity(intent)

        }

        //usera mesaj gönderir
        holder.message.setOnClickListener {
            val intent = Intent(context,ChatActivity::class.java)
            intent.putExtra("userId",basvuranlar[position].userId)
            context.startActivity(intent)
            Toast.makeText(context,"Yakında mesaj gönderilecek",Toast.LENGTH_LONG).show()
        }


        (context as BasvuranlarActivity).basvuranOnay.text = "Onayla - "+kapasaite+" boş koltuk kaldı"

        //bi kişiyi kabul etme onaylama kısmı sürücüler için
        holder.check.setOnClickListener {
            val ilanId = (context as BasvuranlarActivity).ilanId
            //kişi kabul edilmişse statüs durumuna göre durumlar mevcut
            if (!holder.check.isChecked){
                //statüs bilgisi yani onay bilgisi 3 ise ödeme yapılmıştır
                if (basvuranlar[position].onay == 3){
                    Toast.makeText(context,"Ödeme yapmış birini çıkaramazsınız. Lütfen yolcu ile iletişime geçiniz",Toast.LENGTH_LONG).show()
                    holder.check.isChecked =true
                }else{ //ödeme yapmamışsa statüsü 0 a çevirilir ve hiç kabul edilmemeş gibi olur
                    mRef.child("ilanlar").child(ilanId).child("basvuranlar").child(basvuranlar[position].userId).child("statüs").setValue(0)
                            .addOnCompleteListener { p0->
                                if (p0.isSuccessful){
                                    kapasaite++
                                    (context as BasvuranlarActivity).guncelKapasite = kapasaite
                                    (context as BasvuranlarActivity).basvuranOnay.text = "Onayla - "+kapasaite+" boş koltuk kaldı"
                                    (context as BasvuranlarActivity).basvuranlar[position].onay = 0
                                    basvuranlar[position].onay = 1
                                }else{
                                    Toast.makeText(context,"Bir hata oluştu tekrar deneyiniz.",Toast.LENGTH_LONG).show()
                                }
                            }
                }




            }else{
                // eğer birisi seçilmemişse seçilecek demektir ve statüs 1 yapılır
                if (!(kapasaite == 0)){
                    mRef.child("ilanlar").child(ilanId).child("basvuranlar").child(basvuranlar[position].userId).child("statüs").setValue(1)
                            .addOnCompleteListener { p0->
                                if (p0.isSuccessful){
                                    kapasaite--
                                    (context as BasvuranlarActivity).guncelKapasite = kapasaite
                                    (context as BasvuranlarActivity).basvuranOnay.text = "Onayla - "+kapasaite+" boş koltuk kaldı"
                                    (context as BasvuranlarActivity).basvuranlar[position].onay = 1
                                }else{
                                    Toast.makeText(context,"Bir hata oluştu tekrar deneyiniz.",Toast.LENGTH_LONG).show()
                                }
                            }



                }else{
                    Toast.makeText(context,"Tüm koltuklar dolu",Toast.LENGTH_LONG).show()
                    holder.check.isChecked = false
                }

            }

        }




    }

    override fun getItemCount() = basvuranlar.size




    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){




        val adSoyad: TextView = itemView.findViewById(R.id.basvuranAd)
        val userPic: CircleImageView = itemView.findViewById(R.id.basVuranUserPic)
        val parent: CardView = itemView.findViewById(R.id.basvuranView)
        val check : CheckBox = itemView.findViewById(R.id.basvuranCheck)
        val message : ImageView = itemView.findViewById(R.id.basvuranMesaj)



    }
}
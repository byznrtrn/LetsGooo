package com.eskisehir.letsgo.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.eskisehir.letsgo.Model.Ilan
import com.eskisehir.letsgo.Profil.YolcuPuanlamaActivity
import com.eskisehir.letsgo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.collections.ArrayList

class TransferGecmisiAdapter(context: Context, val ilanlar: ArrayList<String>
): RecyclerView.Adapter<TransferGecmisiAdapter.ViewHolder>() {


    lateinit var mRef : DatabaseReference
    lateinit var photoUrl : String
    lateinit var ilan: Ilan
    var isMyIlan = false
    var context = context
    lateinit var currentUserId : String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{

        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.transfer_gecmisi,parent,false)
    //tranfer_gecmisi layoutuyla bağladık
        mRef = FirebaseDatabase.getInstance().reference
        //photoUrl = FirebaseAuth.getInstance().currentUser!!.photoUrl.toString()
        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {


        //gelen ilan bilgisine göre ilanlar çekilir
        mRef.child("ilanlar").child(ilanlar[position]).addValueEventListener(object  : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    ilan = p0.getValue(Ilan::class.java)!!
                    //ilanın user id si current user id ye eşit ise ve biz transfer adapterda olduğumuz için sürücüyüz demektir, diğer türlü yolcu
                    if (ilan.userId!!.split("&")[0].equals(currentUserId)){
                        isMyIlan = true
                        holder.puanVer.text = "Yolcuları puanla"
                    }else{
                        isMyIlan = false
                        holder.puanVer.text = "Sürücüyü Puanla"
                    }

                    mRef.child("users").child(ilan.userId!!.split("&")[0]).child("profile_picture")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    if (p0.exists()){
                                        photoUrl = p0.getValue(String::class.java)!!
                                        UniversalImageLoader.setImage(photoUrl,holder.userPic,null,"")
                                    }

                                }

                            })


                    holder.nerden.text = ilan.rotaDate!!.split("&")[0]
                    holder.nereye.text = ilan.rotaDate!!.split("&")[1]
                    holder.date.text = ilan.rotaDate!!.split("&")[2]
                    holder.time.text = ilan.saat
                    holder.price.text = ilan.price.toString()


                    mRef.child("users").child(ilan.userId!!.split("&")[0]).child("adi_soyadi")//&'dan sonrası true ya da false
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    if (p0.exists()){
                                        holder.name.text = p0.getValue(String::class.java)!!

                                    }

                                }

                            })

                    if (!isMyIlan){
                        mRef.child("users").child(currentUserId).child("puanlar").child("verilenPuan")
                                .child(ilan.userId!!.split("&")[0]).addValueEventListener(object : ValueEventListener{
                                    override fun onCancelled(p0: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }

                                    @RequiresApi(Build.VERSION_CODES.O)
                                    override fun onDataChange(p0: DataSnapshot) {
                                        if (p0.exists()){ //puan gösterilir
                                            println("geldin mi buraya")
                                            holder.tvPuanYok.visibility = View.GONE
                                            holder.rBar.visibility = View.VISIBLE

                                            val puan = p0.getValue(Float::class.java)
                                            holder.rBar.rating = puan!!

                                        }else{ //puan yok
                                            println("ya buraya")

                                            holder.tvPuanYok.visibility = View.VISIBLE
                                            holder.rBar.visibility = View.INVISIBLE
                                        }
                                    }

                                })
                    }






                }
            }//ilk onDataChange metodunun sonu

        })


        holder.puanVer.setOnClickListener {

            if (isMyIlan){ //benim ilanımsa yolcu puanlamaAct.ye gider

                val intent = Intent(context,YolcuPuanlamaActivity::class.java)
                intent.putExtra("ilanId",ilan.ilanId)
                context.startActivity(intent)

            }else{
                val popDialog = AlertDialog.Builder(context)
                var linearLayout = LinearLayout(context)
                val rating = RatingBar(context)

                var lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)

                rating.layoutParams = lp
                rating.numStars = 5
                rating.stepSize = 1F

                linearLayout.addView(rating)
                popDialog.setIcon(ContextCompat.getDrawable(context,R.drawable.rate_star_full))
                popDialog.setTitle("Puan verin")
                popDialog.setView(linearLayout)

                popDialog.setPositiveButton("Onay", object : DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {

                        mRef.child("users").child(currentUserId).child("puanlar").child("verilenPuan")
                                .child(ilan.userId!!.split("&")[0]).setValue(rating.progress)

                        mRef.child("users").child(ilan.userId!!.split("&")[0]).child("puanlar").child("alinanPuan")
                                .child(currentUserId).setValue(rating.progress)

                    }

                })

                popDialog.create()
                popDialog.show()

            }



        }





    }

    override fun getItemCount() = ilanlar.size




    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){




        val nerden: TextView = itemView.findViewById(R.id.ilanNerden)
        val nereye: TextView = itemView.findViewById(R.id.ilanNereye)
        val time: TextView = itemView.findViewById(R.id.ilanSaat)
        val date: TextView = itemView.findViewById(R.id.ilanDate)
        val price: TextView = itemView.findViewById(R.id.ilanFiyat)
        val name: TextView = itemView.findViewById(R.id.ilanAdiSoyadi)
        val userPic: CircleImageView = itemView.findViewById(R.id.ilanUserPic)
        val parent: CardView = itemView.findViewById(R.id.ilanView)
        val puanVer : Button = itemView.findViewById(R.id.surucuyePuanVer)
        val tvPuanYok : TextView = itemView.findViewById(R.id.tvPuanYok)
        val rBar : RatingBar = itemView.findViewById(R.id.rBar)





    }

    /* public fun lastPostReceived(){
         isLast = true
     }*/
}
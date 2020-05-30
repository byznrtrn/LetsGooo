package com.example.letsgoo.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.letsgoo.Model.Ilan
import com.example.letsgoo.Profil.YolcuPuanlamaActivity
import com.example.letsgoo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class YolcuPuanlamaAdapter(context: Context, val users: ArrayList<String>
): RecyclerView.Adapter<YolcuPuanlamaAdapter.ViewHolder>() {


    lateinit var mRef : DatabaseReference
    var context = context
    lateinit var currentUserId : String
    var isFive = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{

        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.yolcu_puanlama,parent,false)

        mRef = FirebaseDatabase.getInstance().reference
        //photoUrl = FirebaseAuth.getInstance().currentUser!!.photoUrl.toString()
        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {

        mRef.child("users").child(users[position]).child("profile_picture").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                println(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()){
                    val photoUrl = p0.getValue(String::class.java)!!
                    UniversalImageLoader.setImage(photoUrl,holder.userPic,null,"")

                }

            }

        })

        mRef.child("users").child(users[position]).child("adi_soyadi").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                println(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()){
                    val adi = p0.getValue(String::class.java)!!
                    holder.name.text = adi

                }

            }

        })

        mRef.child("users").child(currentUserId).child("puanlar").child("verilenPuan")
                .child(users[position]).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            println("geldin mi buraya")
                            holder.tvPuanYok.visibility = View.GONE
                            holder.rBar.visibility = View.VISIBLE

                            val puan = p0.getValue(Float::class.java)
                            holder.rBar.rating = puan!!
                            if (puan == 5f){
                                isFive = true
                            }

                        }else{
                            println("ya buraya")

                            holder.tvPuanYok.visibility = View.VISIBLE
                            holder.rBar.visibility = View.INVISIBLE
                        }
                    }

                })





        holder.puanVer.setOnClickListener {


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
                popDialog.setIcon(ContextCompat.getDrawable(context, R.drawable.rate_star_full))
                popDialog.setTitle("Puan verin")
                popDialog.setView(linearLayout)

                popDialog.setPositiveButton("Onay", object : DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {

                        if (isFive && rating.progress != 5){
                            mRef.child("users").child(users[position]).child("rozetler").child("guvenilir")
                                    .addListenerForSingleValueEvent(object : ValueEventListener{
                                        override fun onCancelled(p0: DatabaseError) {
                                            println(p0.message)
                                        }

                                        override fun onDataChange(p0: DataSnapshot) {
                                            if (p0.exists()){
                                                var guvenilir = p0.getValue(Int::class.java)!!
                                                guvenilir--
                                                mRef.child("users").child(users[position]).child("rozetler").child("guvenilir").setValue(guvenilir)
                                            }
                                        }

                                    })
                        }else if (!isFive && rating.progress == 5){

                            mRef.child("users").child(users[position]).child("rozetler").child("guvenilir")
                                    .addListenerForSingleValueEvent(object : ValueEventListener{
                                        override fun onCancelled(p0: DatabaseError) {
                                            println(p0.message)
                                        }

                                        override fun onDataChange(p0: DataSnapshot) {
                                            if (p0.exists()){
                                                var guvenilir = p0.getValue(Int::class.java)!!
                                                guvenilir++
                                                mRef.child("users").child(users[position]).child("rozetler").child("guvenilir").setValue(guvenilir)
                                            }
                                        }

                                    })


                        }


                        mRef.child("users").child(currentUserId).child("puanlar").child("verilenPuan")
                                .child(users[position]).setValue(rating.progress)

                        mRef.child("users").child(users[position]).child("puanlar").child("alinanPuan")
                                .child(currentUserId).setValue(rating.progress)

                    }

                })

                popDialog.create()
                popDialog.show()

            }



        }


    override fun getItemCount() = users.size




    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){





        val name: TextView = itemView.findViewById(R.id.ilanAdiSoyadi)
        val userPic: CircleImageView = itemView.findViewById(R.id.ilanUserPic)
        val puanVer : Button = itemView.findViewById(R.id.surucuyePuanVer)
        val tvPuanYok : TextView = itemView.findViewById(R.id.tvPuanYok)
        val rBar : RatingBar = itemView.findViewById(R.id.rBar)





    }

    /* public fun lastPostReceived(){
         isLast = true
     }*/
}



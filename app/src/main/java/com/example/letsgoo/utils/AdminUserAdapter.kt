package com.example.letsgoo.utils

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
import com.example.letsgoo.Home.BasvuranlarActivity
import com.example.letsgoo.Message.ChatActivity
import com.example.letsgoo.Model.Basvuranlar
import com.example.letsgoo.Model.Users
import com.example.letsgoo.Profil.AcikProfil
import com.example.letsgoo.R
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

//gelen userlar admin panelinde olduğu gibi klasik adapter yapısında eklendi
class AdminUserAdapter (context: Context, var users: ArrayList<Users>
): RecyclerView.Adapter<AdminUserAdapter.ViewHolder>() {


    lateinit var mRef : DatabaseReference
    var context = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{

        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.admin_user_view,parent,false)
        mRef = FirebaseDatabase.getInstance().reference



        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {

        holder.adSoyad.text = users[position].adi_soyadi
        UniversalImageLoader.setImage(users[position].profile_picture!!,holder.userPic,null,"")
        if (users[position].statüs == 1){
            holder.sil.setImageResource(R.drawable.carpi)
        }else{
            holder.sil.setImageResource(R.drawable.check)
        }

        holder.userPic.setOnClickListener {
            val intent = Intent(context,AcikProfil::class.java)
            intent.putExtra("userId",users[position].user_id)
            context.startActivity(intent)

        }


        holder.sil.setOnClickListener {

            if (users[position].statüs == 1){
                mRef.child("users").child(users[position].user_id!!).child("statüs").setValue(0)
                        .addOnCompleteListener { p0->
                            if (p0.isSuccessful){
                                Toast.makeText(context,"Kullanıcı engellendi",Toast.LENGTH_LONG).show()
                                holder.sil.setImageResource(R.drawable.check)
                                users[position].statüs = 0
                            }
                        }

            }else{
                mRef.child("users").child(users[position].user_id!!).child("statüs").setValue(1)
                        .addOnCompleteListener { p0->
                            if (p0.isSuccessful){
                                Toast.makeText(context,"Engel Kaldırıldı",Toast.LENGTH_LONG).show()
                                holder.sil.setImageResource(R.drawable.carpi)
                                users[position].statüs = 1
                            }
                        }
            }



        }


    }

    override fun getItemCount() = users.size




    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){




        val adSoyad: TextView = itemView.findViewById(R.id.basvuranAd)
        val userPic: CircleImageView = itemView.findViewById(R.id.basVuranUserPic)
        val parent: CardView = itemView.findViewById(R.id.basvuranView)
        val sil : ImageView = itemView.findViewById(R.id.userSil)



    }
}
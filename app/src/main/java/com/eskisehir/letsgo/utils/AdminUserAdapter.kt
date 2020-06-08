package com.eskisehir.letsgo.utils

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.eskisehir.letsgo.Model.Users
import com.eskisehir.letsgo.Profil.AcikProfil
import com.eskisehir.letsgo.R
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

//gelen userlar admin panelinde olduğu gibi klasik adapter yapısında eklendi
class AdminUserAdapter (context: Context, var users: ArrayList<Users>
): RecyclerView.Adapter<AdminUserAdapter.ViewHolder>() {


    lateinit var mRef : DatabaseReference
    var context = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
    //adapter'ımızı admin_user_view ile bağladık LayoutInflater sayesinde
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.admin_user_view,parent,false)
        mRef = FirebaseDatabase.getInstance().reference



        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {

        holder.adSoyad.text = users[position].adi_soyadi
        UniversalImageLoader.setImage(users[position].profile_picture!!,holder.userPic,null,"")
        if (users[position].statüs == 1){ //status=1 olması kullanıcının aktif olduğunu gösterir
            holder.sil.setImageResource(R.drawable.carpi)
        }else{
            holder.sil.setImageResource(R.drawable.check)
        }

        ///burada userid sine göre admin açık profile yönlendiriliyor
        //yollamak istediğim userId,hangi değeri yollamam gerektiği:users
        //position:hangi isme tıklandıysa onu yollucam diğer tarafa
        holder.userPic.setOnClickListener {
            val intent = Intent(context,AcikProfil::class.java)
            intent.putExtra("userId",users[position].user_id)
            context.startActivity(intent)
            //böylece admin engellemeden önce kullanıcıların profil fotosuna tıklayarak profiline bakabilir.

        }


        holder.sil.setOnClickListener { //çarpıya tıkladığımızda olacaklar

            if (users[position].statüs == 1){//eğer kullanıcı aktifse,engellemek için..
                mRef.child("users").child(users[position].user_id!!).child("statüs").setValue(0)
                        .addOnCompleteListener { p0->
                            if (p0.isSuccessful){//status 0 yapılır .bu engellendiği anlamına gelir.
                                Toast.makeText(context,"Kullanıcı engellendi",Toast.LENGTH_LONG).show()
                                holder.sil.setImageResource(R.drawable.check)//ve engellendikten sonra çarpı işareti yerine tik işareti gelir.
                                users[position].statüs = 0
                            }
                        }

            }else{//kullanıcı engelini kaldırmak için status 1 yapılır
                mRef.child("users").child(users[position].user_id!!).child("statüs").setValue(1)
                        .addOnCompleteListener { p0->
                            if (p0.isSuccessful){
                                Toast.makeText(context,"Engel Kaldırıldı",Toast.LENGTH_LONG).show()
                                holder.sil.setImageResource(R.drawable.carpi)//ve tik işareti yerine çarpı işareti gelir.
                                users[position].statüs = 1
                            }
                        }
            }



        }


    }

    override fun getItemCount() = users.size




    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
//RecylerView'in çalışması--> Adapter verileri işler,düzenler ve LayoutManager yardımıyla ekrana basar
//kullanıcı isimleri ve resimleri ekrana basılır


        val adSoyad: TextView = itemView.findViewById(R.id.basvuranAd)
        val userPic: CircleImageView = itemView.findViewById(R.id.basVuranUserPic)
        val parent: CardView = itemView.findViewById(R.id.basvuranView)
        val sil : ImageView = itemView.findViewById(R.id.userSil)



    }
}
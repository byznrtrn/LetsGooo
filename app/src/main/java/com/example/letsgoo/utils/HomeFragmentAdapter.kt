package com.example.letsgoo.utils

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.eskisehir.letsgo.Model.Ilan
import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.Search.IlanDetayActivity
import com.eskisehir.letsgo.Search.SearchResultFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_search.view.*
import org.greenrobot.eventbus.EventBus
import java.util.ArrayList

class HomeFragmentAdapter(context: Context, val ilanlar: ArrayList<Ilan>
): RecyclerView.Adapter<HomeFragmentAdapter.ViewHolder>() {


    lateinit var mRef : DatabaseReference
    lateinit var photoUrl : String
    var context = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{

        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.ilan_sonuc,parent,false)

        mRef = FirebaseDatabase.getInstance().reference
        //photoUrl = FirebaseAuth.getInstance().currentUser!!.photoUrl.toString()

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder , position: Int) {



         mRef.child("users").child(ilanlar[position].userId!!.split("&")[0]).child("profile_picture")
                .addListenerForSingleValueEvent(object : ValueEventListener{
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



        holder.parent.setOnClickListener {

            val intent = Intent(context,IlanDetayActivity::class.java)
            EventBus.getDefault().postSticky(EventbusDataEvents.ilanBilgileriniGonder(ilanlar[position]))
            context.startActivity(intent)


        }










        holder.nerden.text = ilanlar[position].rotaDate!!.split("&")[0]
        holder.nereye.text = ilanlar[position].rotaDate!!.split("&")[1]
        holder.date.text = ilanlar[position].rotaDate!!.split("&")[2]
        holder.time.text = ilanlar[position].saat
        holder.price.text = ilanlar[position].price.toString()
        holder.koltuk.text = ilanlar[position].kapasite.toString() +" koltuk boş"


        mRef.child("users").child(ilanlar[position].userId!!.split("&")[0]).child("adi_soyadi")
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            holder.name.text = p0.getValue(String::class.java)!!

                        }

                    }

                })


    }

    override fun getItemCount() = ilanlar.size




    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){




        val nerden: TextView = itemView.findViewById(R.id.ilanNerden)
        val nereye: TextView = itemView.findViewById(R.id.ilanNereye)
        val time: TextView = itemView.findViewById(R.id.ilanSaat)
        val date: TextView = itemView.findViewById(R.id.ilanDate)
        val price: TextView = itemView.findViewById(R.id.ilanFiyat)
        val name: TextView = itemView.findViewById(R.id.ilanAdiSoyadi)
        val koltuk: TextView = itemView.findViewById(R.id.ilanBosKoltuk)
        val userPic: CircleImageView = itemView.findViewById(R.id.ilanUserPic)
        val parent: CardView = itemView.findViewById(R.id.ilanView)




    }

    /* public fun lastPostReceived(){
         isLast = true
     }*/
}
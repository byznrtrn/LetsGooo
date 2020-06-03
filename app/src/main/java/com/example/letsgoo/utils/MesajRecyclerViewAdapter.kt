package com.example.letsgoo.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.letsgoo.Model.Mesaj
import com.example.letsgoo.Model.Users
import com.example.letsgoo.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.tek_satir_mesaj_alan.view.*

class MesajRecyclerViewAdapter(var tumMesajlar:ArrayList<Mesaj>, var myContext: Context,
                               var sohbetEdilecekUser: Users?) : androidx.recyclerview.widget.RecyclerView.Adapter<MesajRecyclerViewAdapter.MyMesajViewHolder>() {


    //bunlar viewholder sınıfı ile gelen metotlar

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):MyMesajViewHolder {
//bu metot bizden MyMesajViewHolder sınıfı döndürmemizi istiyor

        var myView: View?=null

        //mesaj alan ve gönderenin mesajları farklı yerlerde olacağı için farklı viewlerde gösterildi. Gelen mesaj objesinde bunun türü bulunmakta
        //bu türe göre mesaj sahibi ya da karşı taraf diye view döndürülür
        if(viewType==1){
            //tek_satir_mesaj_gonderen xml'i ile bağlıcaz
            myView= LayoutInflater.from(myContext).inflate(R.layout.tek_satir_mesaj_gonderen,parent,false)
            return MyMesajViewHolder(myView, null)
            //Layoutinflater yapısı bir xml oluşturduğumuzda onu koda bağlamak için oluşturduğumuz yapıdır

        }else  {
            myView= LayoutInflater.from(myContext).inflate(R.layout.tek_satir_mesaj_alan,parent,false)
            return MyMesajViewHolder(myView, sohbetEdilecekUser!!)
        }

    }



    override fun getItemCount(): Int {
        return tumMesajlar.size
        //tumMesajlar listesinde kaç eleman varsa almış olduk
    }

    override fun onBindViewHolder(holder:MyMesajViewHolder, position: Int) {
        //mesaj objesinin verileri adapterda eklenir
        holder.setData(tumMesajlar.get(position))
    }

    override fun getItemViewType(position: Int): Int {
        if(tumMesajlar.get(position).user_id!!.equals(FirebaseAuth.getInstance().currentUser!!.uid)){
            return 1
        }else return 2
    }



    class MyMesajViewHolder(itemView: View?, var sohbetEdilecekUser: Users?) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView!!) {

        var tumlayout=itemView as ConstraintLayout
        var mesajText=tumlayout.tvMesaj
        var profilePicture=tumlayout.mesajUserProfilePic


        fun setData(oankiMesaj: Mesaj) {

            if(sohbetEdilecekUser!=null){
                UniversalImageLoader.setImage(sohbetEdilecekUser!!.profile_picture!!,profilePicture,null,"")
            }

            mesajText.text=oankiMesaj.mesaj
        }

    }


}
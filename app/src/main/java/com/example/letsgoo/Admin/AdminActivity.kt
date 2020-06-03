package com.example.letsgoo.Admin


import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsgoo.Login.FirstActivity
import com.example.letsgoo.Model.Users
import com.example.letsgoo.R
import com.example.letsgoo.utils.AdminUserAdapter
import com.example.letsgoo.utils.UniversalImageLoader
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.activity_admin.*


//tüm userlar veritabanından çekilip bir adaptere atılıp gösterildi. User işlemleri adapterde yazıldı
class AdminActivity : AppCompatActivity() {

    lateinit var mRef : DatabaseReference
    var userList:ArrayList<Users> = ArrayList()
    var qUserList:ArrayList<Users> = ArrayList()

    var searchItem:MenuItem? = null
    var searcView:SearchView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        mRef = FirebaseDatabase.getInstance().reference
        initImageLoader()
        //users kısmından veriler çekiliyor
        mRef.child("users").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                println(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    for (user in p0.children){
                        //mRef.child("users").child(user.child("user_id").getValue(String::class.java)!!).child("statüs").setValue(1)
                        userList.add(user.getValue(Users::class.java)!!)
                    }

                    //recyclerView tanımlanıyor
                    rvUsers.layoutManager = LinearLayoutManager(this@AdminActivity)
                    //LinearLayoutManager = rv'de yaptığımız rowların nasıl dizileceğini gösteriyor:alt alta diziyor
                    val adapter = AdminUserAdapter(this@AdminActivity,userList)
                    rvUsers.adapter = adapter

                }
            }

        })


    }





    //çıkış yapmak için menü tanımlandı
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.admin_menu,menu)
        searchItem = menu!!.findItem(R.id.action_search)
        searcView = menu.findItem(R.id.action_search).actionView as SearchView?


        searcView!!.setOnQueryTextListener(object:SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query:String):Boolean {
                // Toast like print
                return false
            }
            override fun onQueryTextChange(s:String):Boolean {

                // println(s)
                return false
            }
        })
        // searcView = searchItem!!.actionView as SearchView?



        return super.onCreateOptionsMenu(menu)
    }







    // actions on click menu items
    override fun onOptionsItemSelected(item: MenuItem)= when (item.itemId) {



        R.id.cikis -> {
            FirebaseAuth.getInstance().signOut()

            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener {
                        startActivity(Intent(this, FirstActivity::class.java))
                        finish()
                    }
            true
        }

        else -> {


            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }











   /* override fun onOptionsItemSelected(item: MenuItem): Boolean {



        when (item.itemId) {
            R.id.cikis -> {
                FirebaseAuth.getInstance().signOut()

                AuthUI.getInstance().signOut(this)
                        .addOnCompleteListener {
                            startActivity(Intent(this, FirstActivity::class.java))
                            finish()
                        }

            }




            else -> {
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu):Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.admin_menu,menu)
        val searchItem = menu!!.findItem(R.id.action_search)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView?

       /* menu.clear()
        menuInflater.inflate(R.menu.admin_menu,menu)
        val searchView = SearchView(supportActionBar?.themedContext ?: this)
        menu.findItem(R.id.action_search).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItem.SHOW_AS_ACTION_IF_ROOM)
            actionView = searchView
        }*/

        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {


                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {

                if (newText.length == 0){
                    rvUsers!!.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@AdminActivity)
                    rvUsers.adapter = AdminUserAdapter(this@AdminActivity,userList)
                }else{
                    val Text = newText.toLowerCase()
                    qUserList.clear()




                    for (user in userList){
                        if (user.adi_soyadi!!.toLowerCase().contains(Text) && !qUserList.contains(user)){
                            qUserList.add(user)
                        }
                    }

                    if (qUserList.isEmpty()) {
                        var toast = Toast.makeText(this@AdminActivity,"Lütfen aramadan önce arama yapılacak içeriği seçiniz.", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL,0,0)
                        toast.show()
                    }

                    rvUsers!!.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@AdminActivity)
                    rvUsers.adapter = AdminUserAdapter(this@AdminActivity,qUserList)
                }



                return false
            }
        })

        return false
    }*/


    private fun initImageLoader(){
        val universalImageLoader = UniversalImageLoader(this)
        ImageLoader.getInstance().init(universalImageLoader.config)
    }


}

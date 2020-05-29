package com.example.letsgoo.Profil

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.eskisehir.letsgo.Model.Users

import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.EventbusDataEvents
import com.eskisehir.letsgo.utils.UniversalImageLoader
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import org.greenrobot.eventbus.EventBus

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {

    lateinit var firstViewPager: ViewPager
    lateinit var tabLayout: TabLayout
    lateinit var mUser: FirebaseUser
    lateinit var mAuth : FirebaseAuth
    lateinit var mRef : DatabaseReference
    lateinit var rootView:View
    lateinit var okunanKullaniciBilgileri:Users

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView =  inflater.inflate(R.layout.fragment_profile, container, false)

        firstViewPager = rootView.findViewById(R.id.profileViewPager) as ViewPager
        tabLayout = rootView.findViewById(R.id.profileTabLayout) as TabLayout
        tabLayout!!.setupWithViewPager(firstViewPager)
        // tabLayout!!.setSelectedTabIndicatorHeight(Color.parseColor("#358ecd"))
        setupViewPager(firstViewPager!!)
        firstViewPager!!.offscreenPageLimit=2



        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser!!
        mRef = FirebaseDatabase.getInstance().reference

        kullaniciBilgileriniGetir()





        return rootView
    }
 //user bilgileri veritabanından çekilir ve ilgili yerler doldurulur
    private fun kullaniciBilgileriniGetir() {

        mRef.child("users").child(mUser.uid).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    okunanKullaniciBilgileri = p0.getValue(Users::class.java)!!
                    rootView.profilNameSurname.text = okunanKullaniciBilgileri!!.adi_soyadi
                    val imgURL = okunanKullaniciBilgileri.profile_picture
                    UniversalImageLoader.setImage(imgURL!!,profileUserPic!!,progressBar,"")
                }

            }

        })

    }


    //profil sayfasından ki iki fragment pager yapısına ekleniyor
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = MyViewPagerAdapter(childFragmentManager)
        adapter.addFragment(MyInfoFragment(),"Bilgilerim")
        adapter.addFragment(AccountFragment(),"Hesap")
        viewPager.setAdapter(adapter)

        viewPager.offscreenPageLimit = 2


    }

    //pager yapısı için adapter (hazır kod)
    class MyViewPagerAdapter(manager : FragmentManager) : FragmentPagerAdapter(manager) {

        private val fragmentList: MutableList<Fragment> = ArrayList()
        private val titleList: MutableList<String> = ArrayList()


        override fun getItem(position: Int): Fragment{
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }


        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            titleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titleList[position]
        }

    }


}

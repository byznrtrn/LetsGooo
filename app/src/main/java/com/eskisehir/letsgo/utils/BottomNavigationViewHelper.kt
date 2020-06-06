package com.eskisehir.letsgo.utils

import android.content.Context
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.eskisehir.letsgo.Add.AddFragment
import com.eskisehir.letsgo.Home.HomeFragment
import com.eskisehir.letsgo.Home.MainActivity
import com.eskisehir.letsgo.Message.MessageFragment
import com.eskisehir.letsgo.Profil.ProfileFragment
import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.Search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx

class BottomNavigationViewHelper {


    companion object{
//fragmentler tanımlandı
        internal val fragment1: Fragment = HomeFragment()
        internal val fragment2: Fragment = SearchFragment()
        internal val fragment3: Fragment = AddFragment()
        internal val fragment4: Fragment = ProfileFragment()
        internal val fragment5: Fragment = MessageFragment()
        lateinit var fm :FragmentManager
        internal var active = fragment1


        var currentFragmentNo = 0
        fun setupBottomNavigationView(bottomNavigationViewEx: BottomNavigationViewEx){

            bottomNavigationViewEx.enableAnimation(false)
            bottomNavigationViewEx.enableShiftingMode(false)
            bottomNavigationViewEx.enableItemShiftingMode(false)
            bottomNavigationViewEx.setTextVisibility(false)




        }

        fun setupNavigation(context: Context, bottomNavigationViewEx: BottomNavigationViewEx){

            fm = (context as MainActivity).supportFragmentManager

            //tüm fragmentler açılışta yüklensin diye hızlıca sondan başa geçiş yapıldı
            fm.beginTransaction().add(R.id.mainContainer, fragment5, "5").hide(fragment5).commit()
            fm.beginTransaction().add(R.id.mainContainer, fragment4, "4").hide(fragment4).commit()
            fm.beginTransaction().add(R.id.mainContainer, fragment3, "3").hide(fragment3).commit()
            fm.beginTransaction().add(R.id.mainContainer, fragment2, "2").hide(fragment2).commit()
            fm.beginTransaction().add(R.id.mainContainer, fragment1, "1").commit()


            //değişikler için fragmentler arası geçiş yapıldı
            bottomNavigationViewEx.onNavigationItemSelectedListener=object : BottomNavigationView.OnNavigationItemSelectedListener{
                override fun onNavigationItemSelected(item: MenuItem): Boolean {
                    when(item.itemId){

                        R.id.ic_home -> {
                            fm.beginTransaction().hide(active).show(fragment1).commit()
                            active = fragment1

                            return true

                        }

                        R.id.ic_search -> {
                            fm.beginTransaction().hide(active).show(fragment2).commit()
                            active = fragment2
                            return true
                        }

                        R.id.ic_add -> {
                            fm.beginTransaction().hide(active).show(fragment3).commit()
                            active = fragment3

                            return true
                        }

                        R.id.ic_message -> {

                            fm.beginTransaction().hide(active).show(fragment5).commit()
                            active = fragment5


                            return true
                        }

                        R.id.ic_profile -> {
                            fm.beginTransaction().hide(active).show(fragment4).commit()
                            active = fragment4

                            return true
                        }




                    }
                    return false
                }

            }

        }


    }
}
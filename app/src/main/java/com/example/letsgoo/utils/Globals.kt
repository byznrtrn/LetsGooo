package com.example.letsgoo.utils

import com.eskisehir.letsgo.Home.HomeFragment

class Globals {

    //bazı yerlerde farklı fragmente erişim gerekiyor bu yüzden gereken yerde kullanılmak üzere buraya değişken tanımlamaları yapıldı


    companion object letsGo{
        var homeFragment : HomeFragment? = null


        fun returnHome():HomeFragment?{
            return homeFragment
        }
    }
}
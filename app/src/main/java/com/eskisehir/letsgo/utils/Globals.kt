package com.eskisehir.letsgo.utils

import com.eskisehir.letsgo.Home.HomeFragment

class Globals {

    //bazı yerlerde farklı fragmente erişim gerekiyor bu yüzden gereken yerde kullanılmak üzere buraya değişken tanımlamaları yapıldı

//companion object=singleton
    companion object letsGo{
        var homeFragment : HomeFragment? = null
//tek bir homefragment objesi oluşturup buna HomeFragment sayfasında erişip değiştiriyoruz

        fun returnHome():HomeFragment?{
            return homeFragment
        }
    }
}
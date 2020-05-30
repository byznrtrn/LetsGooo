package com.example.letsgoo.utils

import com.example.letsgoo.Model.Car
import com.example.letsgoo.Model.Ilan
import com.example.letsgoo.Model.Users

class EventbusDataEvents {

    //veri gönderirken kullaılan event bus model classı

    internal class KayitBilgileriniGonder(var email:String?, var sifre:String? )

    internal class KullaniciBilgileriniGonder(var kullanici: Users?)

    internal class arabaBilgileriniGonder(var car: Car?, var mode:String?)

    internal class lokasyonBilgileriniGonder(var City: String?, var lg:Double?, var lt:Double?)

    internal class aramaBilgileriniGonder(var nerden:String?,var nereye:String?,var date:String,var lg:Double?, var lt:Double?)

    internal class ilanBilgileriniGonder(var ilan : Ilan?)

    internal class modeGonder(var mode : String?)

    internal class aramaSonucBilgileriniGonder(var ilanlar:ArrayList<Ilan>)

}
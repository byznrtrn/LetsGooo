package com.eskisehir.letsgo.utils

import com.eskisehir.letsgo.Model.Car
import com.eskisehir.letsgo.Model.Ilan
import com.eskisehir.letsgo.Model.Users

class EventbusDataEvents {

    //veri gönderirken kullaılan event bus model classı

    internal class KayitBilgileriniGonder(var email:String?, var sifre:String? )
    //kayıtbilgilerinigönder dediğimizde email ve şifre ekran görüntüsünü alıyoruz

    internal class KullaniciBilgileriniGonder(var kullanici: Users?)
//kullanıcıbilgilerini gönder dediğimizde kullanıcıların ekran görüntüsünü alıyoruz
    internal class arabaBilgileriniGonder(var car: Car?, var mode:String?)
//arababilgilerinigönder dediğimizde araba ve model verilerinin ekran görüntüsünü alıyoruz
    internal class lokasyonBilgileriniGonder(var City: String?, var lg:Double?, var lt:Double?)
//lokasyonbilgilerini gönderdediğimizde şehir ve koordinat bilgilerinin ekran görüntüsünü alıyoruz
    internal class aramaBilgileriniGonder(var nerden:String?,var nereye:String?,var date:String,var lg:Double?, var lt:Double?)
//nerden nereye gün ve koordinat verilerinin ekran görüntüsünü alıyoruz
    internal class ilanBilgileriniGonder(var ilan : Ilan?)
//ilanların ekran görüntüsünü alıyoruz
    internal class modeGonder(var mode : String?)

    internal class aramaSonucBilgileriniGonder(var ilanlar:ArrayList<Ilan>)
//ilanların liste olarak ekran görüntüsünü alıyoruz
}
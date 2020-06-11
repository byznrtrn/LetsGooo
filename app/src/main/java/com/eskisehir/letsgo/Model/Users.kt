package com.eskisehir.letsgo.Model

class Users {
    //kullanıcının kişisel bilgileri null olarak oluşturuldu
    var email: String? = null
    var username: String? = null
    var adi_soyadi: String? = null
    var cinsiyet : String? = null
    var phone_number: String? = null
    var user_id: String? = null
    var profile_picture:String? = null
    var ozgecmis:String? = null
    var dogumYili : String? = null
    var bakiye:Int? = null
    var rozetler:Rozetler ? = null
    var car:Car? = null
    var tercihler:Tercihler? = null
    var statüs : Int? = null

//kullanıcı bilgilerinin kullanıldığı sınıflarda bu değerleri değiştirebilmek için bunları constructor oluşturup ona atıyoruz
    constructor() {}
    constructor(email: String?, username: String?, adi_soyadi: String?,cinsiyet:String?, phone_number: String?, user_id: String?,profile_picture:String?,ozgecmis:String?,dogumYili:String?,bakiye:Int?, rozetler: Rozetler?,car:Car?,tercihler:Tercihler?,statüs:Int?) {
        this.email = email
        this.username = username
        this.adi_soyadi = adi_soyadi
        this.cinsiyet = cinsiyet
        this.phone_number = phone_number
        this.user_id = user_id
        this.profile_picture = profile_picture
        this.ozgecmis = ozgecmis
        this.dogumYili = dogumYili
        this.bakiye = bakiye
        this.rozetler = rozetler
        this.car = car
        this.tercihler = tercihler
        this.statüs = statüs
    }



}
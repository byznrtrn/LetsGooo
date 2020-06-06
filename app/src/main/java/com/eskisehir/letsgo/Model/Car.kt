package com.eskisehir.letsgo.Model

class Car {

    var marka : String? = null
    var model : String? = null
    var renk : String? = null
    var yil : String? = null
    var plaka : String? = null
    var kapasite : Int? = null
    var pic : String? = null

    constructor(){ }

    constructor(marka: String?, model: String?, renk: String?, yil: String?, plaka: String?, kapasite:Int?,pic:String?) {
        this.marka = marka
        this.model = model
        this.renk = renk
        this.yil = yil
        this.plaka = plaka
        this.kapasite = kapasite
        this.pic = pic

    }
}
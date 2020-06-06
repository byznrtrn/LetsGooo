package com.eskisehir.letsgo.Model

class Ilan {

    var userId: String? = null
    var ilanId: String? = null
    var rotaDate: String? = null
    var saat : String? = null
    var kapasite : Int? = null
    var price: Int? = null
    var unix : Long? = null
    var lt : Double? = null
    var lg : Double? = null



    constructor(){}
    constructor(userId:String,ilanId:String,rotaDate:String,saat:String,kapasite:Int,price:Int,lt:Double,lg:Double,unix:Long){
        this.userId = userId
        this.ilanId = ilanId
        this.rotaDate = rotaDate
        this.saat = saat
        this.kapasite = kapasite
        this.price = price
        this.lt = lt
        this.lg = lg
        this.unix = unix

    }

}
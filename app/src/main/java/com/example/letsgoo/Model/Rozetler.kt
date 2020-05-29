package com.example.letsgoo.Model

class Rozetler {


    var egitim : Int = 0
    var guvenilir : Int = 0
    var gezici : Int = 0
    var gozlemci : Int = 0
    var sofor : Int = 0

    constructor(){ }

    constructor(egitim: Int, guvenilir: Int, gezici: Int, gozlemci: Int, sofor: Int) {
        this.egitim = egitim
        this.guvenilir = guvenilir
        this.gezici = gezici
        this.gozlemci = gozlemci
        this.sofor = sofor

    }


}
package com.example.flamingohackathon2020

import java.io.Serializable

class RandomObject(lat:Double,lon:Double,type:String,ID:String,owner:String,height:Double): Serializable {

    var longitude:Double
    var latitude:Double

    var type:String
    var id:String
    var owner:String

    var height:Double

    init {
        this.latitude = lat
        this.longitude = lon
        this.type = type
        this.id = ID
        this.owner = owner
        this.height = height
    }


}
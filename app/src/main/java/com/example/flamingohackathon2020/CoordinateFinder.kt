package com.example.flamingohackathon2020

///This class finds a new pair of coordinates based on given ones
class CoordinateFinder(lat:Double,lon:Double){

    var latitude:Double
    var longitude:Double

    //radius of the eart in km
    val radius = 63710.01

    init {
        this.latitude = lat
        this.longitude = lon
    }

    fun newCoordinate(angle:Double, distance:Double):Pair<Double,Double>{

        var lat = toRad(this.latitude)
        var lon = toRad(this.longitude)
        var dist = distance/radius
        var bearing = toRad(angle)

        var lat2 = Math.asin( Math.sin(lat)*Math.cos(dist) + Math.cos(lat)*Math.sin(dist)*Math.cos(bearing) );
        var lon2 = lon + Math.atan2(Math.sin(bearing)*Math.sin(dist)*Math.cos(lat), Math.cos(dist)-Math.sin(lat)*Math.sin(lat2));
        lon2 = (lon2+3*Math.PI).rem(2*Math.PI) - Math.PI



        return Pair<Double,Double>(toDeg(lat2),toDeg(lon2))
    }

    fun toRad(deg:Double):Double{
        return deg * Math.PI / 180;
    }
    fun toDeg(rad:Double):Double{
        return rad * 180 / Math.PI;
    }

}
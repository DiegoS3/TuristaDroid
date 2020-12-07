package android.com.diego.turistadroid.bbdd

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Place (@PrimaryKey var id : Long = 0,
                  var nombre : String = "",
                  var fecha : Date = Date(),
                  var city : String = "",
                  var puntuacion : Double = 0.0,
                  var longitud : Double = 0.0,
                  var latitud : Double = 0.0,
                  var imagenes : RealmList<Image> = RealmList()) : RealmObject(){
    constructor(nombre: String, fecha: Date,city: String, puntuacion: Double, longitud: Double, latitud: Double) :
            this((System.currentTimeMillis() / 1000L), nombre, fecha, city, puntuacion, longitud, latitud)
}


package android.com.diego.turistadroid.bbdd.realm.entities

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.*

open class Place (@PrimaryKey var id : Long = 0,
                  var nombre : String = "",
                  var fecha : Date = Date(),
                  var city : String = "",
                  var puntuacion : Double = 0.0,
                  var longitud : Double = 0.0,
                  var latitud : Double = 0.0,
                  var imagenes : RealmList<Image> = RealmList()) : RealmObject(), Serializable




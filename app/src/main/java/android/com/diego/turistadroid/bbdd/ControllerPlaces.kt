package android.com.diego.turistadroid.bbdd

import io.realm.Realm
import io.realm.RealmObject
import io.realm.kotlin.createObject
import io.realm.kotlin.where

object ControllerPlaces {

    private const val DISTANCIA = 0.014000

    //QUERIES TABLA PLACE

    //Insertar un lugar
    fun insertPlace(place: Place){
        Realm.getDefaultInstance().executeTransaction{
            it.copyToRealm(place)
        }
    }

    //Seleccionar todos los lugares
    fun selectPlaces(): MutableList<Place>?{
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<Place>().findAll()
        )
    }

    //Seleccionar lugares cerca de mi
    fun selectNearby(latitud : Double, longitud : Double): MutableList<Place>?{
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<Place>().between("longitud", (longitud - DISTANCIA), (longitud + DISTANCIA)).and()
                .between("latitud", (latitud - DISTANCIA), (latitud + DISTANCIA)).findAll()

        )
    }

    //Actualizar un lugar
    fun updatePlace(place: Place){
        Realm.getDefaultInstance().executeTransaction{
            it.copyToRealmOrUpdate(place)
        }
    }
    //Eliminar todos las imagenes
    fun deleteAllPlaces(){
        Realm.getDefaultInstance().executeTransaction{
            it.where<Place>().findAll().deleteAllFromRealm()
        }
    }

    //Eliminar un lugar
    fun deletePlace(id : Long){
        Realm.getDefaultInstance().executeTransaction{
            it.where<Place>().equalTo("id", id).findFirst()?.deleteFromRealm()
        }
    }

    //Generar AutoIncrement ID
    fun getPlaceIdentity(): Long {
        val registro = Realm.getDefaultInstance().where<Place>().max("id")
        return if (registro == null) {
            1
        } else {
            registro.toLong() + 1
        }
    }
}
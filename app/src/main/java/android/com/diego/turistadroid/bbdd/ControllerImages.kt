package android.com.diego.turistadroid.bbdd

import io.realm.Realm
import io.realm.kotlin.where

object ControllerImages {

    //QUERIES TABLA IMAGE

    //Insertar una imagen
    fun insertImage(image: Image){
        Realm.getDefaultInstance().executeTransaction{
            it.copyToRealm(image)
        }
    }

    //Eliminar una imagen
    fun deleteImage(id : Long){
        Realm.getDefaultInstance().executeTransaction{
            it.where<Image>().equalTo("id", id).findFirst()?.deleteFromRealm()
        }
    }

    //Eliminar todos las imagenes
    fun deleteAllImages(){
        Realm.getDefaultInstance().executeTransaction{
            it.where<Image>().findAll().deleteAllFromRealm()
        }
    }

    //Seleccionar todos las imagenes
    fun selectImages(): MutableList<Image>?{
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<Image>().findAll()
        )
    }

    //Generar codigo imagen autoincrementable
    fun getImageIdentity(): Long {
        val registro = Realm.getDefaultInstance().where<Image>().max("id")
        return if (registro == null) { //si no devuele ningun registro
            1
        } else {
            //Si devuelve registro lo incrementamos en 1
            registro.toLong() + 1
        }
    }

}
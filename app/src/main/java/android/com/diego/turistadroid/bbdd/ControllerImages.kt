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

    fun getImageIdentity(): Long {
        val registro = Realm.getDefaultInstance().where<Image>().max("id")
        return if (registro == null) {
            1
        } else {
            registro.toLong() + 1
        }
    }

}